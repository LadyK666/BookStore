import React, { useEffect, useMemo, useState } from 'react';
import {
  Layout,
  Typography,
  Table,
  Input,
  Button,
  Space,
  Tag,
  message,
  Modal,
  Descriptions,
  Statistic,
  Row,
  Col,
  Badge,
  Select,
  Form,
  Checkbox,
  Popconfirm,
  InputNumber
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { ShoppingCartOutlined, LogoutOutlined, BellOutlined } from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { http } from '../../api/http';

const { Header, Content, Sider, Footer } = Layout;
const { Title, Text } = Typography;

interface BookDto {
  bookId: string;
  isbn?: string;
  title: string;
  publisher?: string;
  publishDate?: string;
  edition?: string;
  price: number;
  status?: string;
  coverImageUrl?: string | null;
  catalog?: string | null;
}

interface CartItem {
  bookId: string;
  title: string;
  quantity: number;
  unitPrice: number;
}

interface LocationState {
  customerId: number;
  customerName: string;
}

interface CustomerSummary {
  customerId: number;
  username: string;
  realName: string;
  mobilePhone?: string;
  email?: string;
  accountBalance: number;
  creditLevelId: number;
  creditLevelName: string;
  discountRate: number;
  privilegeText: string;
}

interface SalesOrderDto {
  orderId: number;
  customerId: number;
  orderTime: string;
  orderStatus: string;
  payableAmount: number;
}

interface OrderDetailItem {
  orderItemId: number;
  bookId: string;
  quantity: number;
  unitPrice: number;
  subAmount: number;
  shippedQuantity?: number | null;
  receivedQuantity?: number | null;
}

interface ShipmentDto {
  shipmentId: number;
  carrier: string;
  trackingNumber: string;
  shipTime?: string | null;
}

interface OrderDetailResp {
  order: SalesOrderDto;
  items: OrderDetailItem[];
  shipments: ShipmentDto[];
}

interface CustomerAddressDto {
  addressId: number;
  customerId: number;
  receiver: string;
  phone?: string;
  province?: string;
  city?: string;
  district?: string;
  detail: string;
  isDefault: boolean;
}

interface CustomerNotificationDto {
  notificationId: number;
  customerId: number;
  orderId?: number | null;
  type: string;
  title: string;
  content: string;
  createdTime?: string | null;
  readFlag: boolean;
}

interface ShortageItemDto {
  orderItemId: number;
  bookId: string;
  quantity: number;
  currentStock: number;
}

const CustomerLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state || {}) as Partial<LocationState>;
  const customerId = state.customerId;
  const customerName = state.customerName || '顾客';

  const [books, setBooks] = useState<BookDto[]>([]);
  const [loadingBooks, setLoadingBooks] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [cart, setCart] = useState<CartItem[]>([]);
  const [detailBook, setDetailBook] = useState<BookDto | null>(null);
  const [summary, setSummary] = useState<CustomerSummary | null>(null);
  const [ordersVisible, setOrdersVisible] = useState(false);
  const [orders, setOrders] = useState<SalesOrderDto[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [orderStatusFilter, setOrderStatusFilter] = useState<string>('全部');
  const [activeOrderDetail, setActiveOrderDetail] = useState<OrderDetailResp | null>(null);
  const [receiveModalVisible, setReceiveModalVisible] = useState(false);
  const [receiveQuantities, setReceiveQuantities] = useState<Record<number, number>>({});
  const [addressModalVisible, setAddressModalVisible] = useState(false);
  const [addresses, setAddresses] = useState<CustomerAddressDto[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState(false);
  const [addressFormVisible, setAddressFormVisible] = useState(false);
  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [notificationsVisible, setNotificationsVisible] = useState(false);
  const [notifications, setNotifications] = useState<CustomerNotificationDto[]>([]);
  const [loadingNotifications, setLoadingNotifications] = useState(false);
  const [shortageModalVisible, setShortageModalVisible] = useState(false);
  const [shortages, setShortages] = useState<ShortageItemDto[]>([]);
  const [shortageNote, setShortageNote] = useState('');
  const [shortageOrder, setShortageOrder] = useState<SalesOrderDto | null>(null);

  useEffect(() => {
    if (!customerId) {
      message.warning('登录信息已丢失，请重新登录');
      navigate('/login');
      return;
    }
    loadSummary();
    loadAllBooks();
  }, [customerId]);

  const loadSummary = async () => {
    try {
      const resp = await http.get<CustomerSummary>(`/customer/${customerId}/summary`);
      setSummary(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载账户信息失败');
    }
  };

  const loadOrders = async (status: string) => {
    if (!customerId) return;
    try {
      setLoadingOrders(true);
      const resp = await http.get<SalesOrderDto[]>(`/customer/${customerId}/orders`, {
        params: { status }
      });
      setOrders(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单失败');
    } finally {
      setLoadingOrders(false);
    }
  };

  const openReceiveModal = async (orderId: number) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/customer/orders/${orderId}`);
      setActiveOrderDetail(resp.data);
      const map: Record<number, number> = {};
      resp.data.items.forEach((item) => {
        const shipped = item.shippedQuantity ?? 0;
        const received = item.receivedQuantity ?? 0;
        const remain = shipped - received;
        if (remain > 0) {
          map[item.orderItemId] = remain;
        }
      });
      if (Object.keys(map).length === 0) {
        message.info('当前订单暂无可确认收货的数量');
        return;
      }
      setReceiveQuantities(map);
      setReceiveModalVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单详情失败');
    }
  };

  const handleConfirmReceive = async () => {
    if (!activeOrderDetail) return;
    const payload: Record<number, number> = {};
    let hasPositive = false;
    activeOrderDetail.items.forEach((item) => {
      const v = receiveQuantities[item.orderItemId];
      if (v && v > 0) {
        hasPositive = true;
        payload[item.orderItemId] = v;
      }
    });
    if (!hasPositive) {
      message.warning('请至少为一条明细填写大于 0 的收货数量');
      return;
    }
    try {
      await http.post(
        `/customer/orders/${activeOrderDetail.order.orderId}/receive`,
        payload
      );
      message.success('确认收货成功');
      setReceiveModalVisible(false);
      await loadOrders(orderStatusFilter);
      await openOrderDetail(activeOrderDetail.order.orderId);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '确认收货失败');
    }
  };

  const loadAddresses = async () => {
    if (!customerId) return;
    try {
      setLoadingAddresses(true);
      const resp = await http.get<CustomerAddressDto[]>(`/customer/${customerId}/addresses`);
      setAddresses(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载地址列表失败');
    } finally {
      setLoadingAddresses(false);
    }
  };

  const loadAllBooks = async () => {
    try {
      setLoadingBooks(true);
      const resp = await http.get<BookDto[]>('/customer/books');
      setBooks(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载书目失败');
    } finally {
      setLoadingBooks(false);
    }
  };

  const searchBooks = async () => {
    const kw = keyword.trim();
    if (!kw) {
      loadAllBooks();
      return;
    }
    try {
      setLoadingBooks(true);
      const resp = await http.get<BookDto[]>('/customer/books/search', { params: { keyword: kw } });
      setBooks(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '搜索失败');
    } finally {
      setLoadingBooks(false);
    }
  };

  const addToCart = (book: BookDto) => {
    Modal.confirm({
      title: `加入《${book.title}》到购物车`,
      content: (
        <Input
          id="cart-qty-input"
          type="number"
          min={1}
          defaultValue={1}
          addonAfter="本"
        />
      ),
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        const input = document.getElementById('cart-qty-input') as HTMLInputElement | null;
        const raw = input?.value || '1';
        const qty = parseInt(raw, 10);
        if (Number.isNaN(qty) || qty <= 0) {
          message.warning('数量必须是正整数');
          return Promise.reject();
        }
        setCart((prev) => {
          const exist = prev.find((i) => i.bookId === book.bookId);
          if (exist) {
            return prev.map((i) =>
              i.bookId === book.bookId ? { ...i, quantity: i.quantity + qty } : i
            );
          }
          return [
            ...prev,
            {
              bookId: book.bookId,
              title: book.title,
              quantity: qty,
              unitPrice: Number(book.price)
            }
          ];
        });
        message.success(`已将 ${qty} 本《${book.title}》加入购物车`);
        return Promise.resolve();
      }
    });
  };

  const removeFromCart = (bookId: string) => {
    setCart((prev) => prev.filter((i) => i.bookId !== bookId));
  };

  const clearCart = () => {
    setCart([]);
  };

  const totalAmount = useMemo(() => {
    const discount = summary?.discountRate ?? 1;
    return cart.reduce((sum, item) => sum + item.quantity * item.unitPrice * discount, 0);
  }, [cart, summary]);

  const bookColumns: ColumnsType<BookDto> = [
    {
      title: '书号',
      dataIndex: 'bookId',
      width: 100,
      render: (val, record) => (
        <Button type="link" size="small" onClick={() => setDetailBook(record)}>
          {val}
        </Button>
      )
    },
    { title: '书名', dataIndex: 'title', width: 220 },
    { title: '出版社', dataIndex: 'publisher', width: 160 },
    {
      title: '定价',
      dataIndex: 'price',
      width: 90,
      render: (v: number) => `¥${v?.toFixed(2)}`
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (v?: string) =>
        v ? <Tag color={v === 'AVAILABLE' ? 'green' : 'red'}>{v}</Tag> : null
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          type="primary"
          size="small"
          icon={<ShoppingCartOutlined />}
          onClick={() => addToCart(record)}
        >
          加入
        </Button>
      )
    }
  ];

  const cartColumns: ColumnsType<CartItem> = [
    { title: '书名', dataIndex: 'title', width: 180 },
    { title: '数量', dataIndex: 'quantity', width: 70 },
    {
      title: '小计',
      key: 'subtotal',
      width: 90,
      render: (_, r) => `¥${(r.unitPrice * r.quantity).toFixed(2)}`
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_, r) => (
        <Button danger size="small" onClick={() => removeFromCart(r.bookId)}>
          删除
        </Button>
      )
    }
  ];

  const orderColumns: ColumnsType<SalesOrderDto> = [
    {
      title: '订单号',
      dataIndex: 'orderId',
      render: (val, record) => (
        <Button type="link" size="small" onClick={() => openOrderDetail(record.orderId)}>
          {val}
        </Button>
      )
    },
    {
      title: '下单时间',
      dataIndex: 'orderTime',
      width: 180
    },
    {
      title: '状态',
      dataIndex: 'orderStatus',
      render: (v: string) => <Tag>{v}</Tag>
    },
    {
      title: '金额',
      dataIndex: 'payableAmount',
      render: (v: number) => `¥${v?.toFixed(2)}`
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      render: (_, record) => {
        const canPay =
          record.orderStatus === 'PENDING_PAYMENT' ||
          record.orderStatus === 'OUT_OF_STOCK_PENDING';
        const canReceive =
          record.orderStatus === 'DELIVERING' || record.orderStatus === 'SHIPPED';
        return (
          <Space>
            <Button
              type="primary"
              size="small"
              disabled={!canPay}
              onClick={() => payOrder(record.orderId)}
            >
              付款
            </Button>
            <Button
              size="small"
              disabled={!canReceive}
              onClick={() => openReceiveModal(record.orderId)}
            >
              确认收货
            </Button>
          </Space>
        );
      }
    }
  ];

  const openOrders = async () => {
    setOrdersVisible(true);
    await loadOrders(orderStatusFilter);
  };

  const openOrderDetail = async (orderId: number) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/customer/orders/${orderId}`);
      setActiveOrderDetail(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单详情失败');
    }
  };

  const payOrder = async (orderId: number) => {
    try {
      await http.post(`/customer/orders/${orderId}/pay`);
      message.success('付款成功');
      // 刷新账户余额与订单列表
      await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
      // 如果详情正好是这个订单，重新拉一下详情
      if (activeOrderDetail?.order.orderId === orderId) {
        await openOrderDetail(orderId);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '付款失败');
    }
  };

  const logout = () => {
    navigate('/login');
  };

  const openAddressModal = async () => {
    if (!customerId) return;
    setAddressModalVisible(true);
    await loadAddresses();
  };

  const handleAddAddress = async (values: any) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/addresses`, {
        receiver: values.receiver,
        phone: values.phone,
        province: values.province,
        city: values.city,
        district: values.district,
        detail: values.detail,
        isDefault: values.isDefault || false
      });
      message.success('地址已添加');
      setAddressFormVisible(false);
      await loadAddresses();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '新增地址失败');
    }
  };

  const setDefaultAddress = async (addressId: number) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/addresses/${addressId}/default`);
      message.success('已设为默认地址');
      await loadAddresses();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '设置默认地址失败');
    }
  };

  const deleteAddress = async (addressId: number) => {
    try {
      await http.delete(`/customer/${customerId}/addresses/${addressId}`);
      message.success('地址已删除');
      await loadAddresses();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除地址失败');
    }
  };

  const submitOrder = async () => {
    if (cart.length === 0) {
      message.warning('购物车为空，请先添加商品');
      return;
    }
    if (!customerId) {
      message.warning('登录信息已丢失，请重新登录');
      navigate('/login');
      return;
    }
    try {
      // 构建地址快照：优先使用默认地址或唯一地址，否则退回到顾客姓名
      let snapshot = '';
      if (addresses.length > 0) {
        const defaultAddr = addresses.find((a) => a.isDefault) || addresses[0];
        snapshot =
          (defaultAddr.province || '') +
          (defaultAddr.city || '') +
          (defaultAddr.district || '') +
          (defaultAddr.detail || '');
        if (defaultAddr.receiver) {
          snapshot = `${defaultAddr.receiver}，${snapshot}`;
        }
      } else if (summary) {
        snapshot = summary.realName || summary.username;
      }

      const resp = await http.post<SalesOrderDto>(`/customer/${customerId}/orders`, {
        items: cart.map((c) => ({
          bookId: c.bookId,
          quantity: c.quantity,
          unitPrice: c.unitPrice
        })),
        shippingAddressSnapshot: snapshot,
        customerNote: undefined
      });

      const order = resp.data;
      // 下单成功后，先检查是否存在缺书项
      try {
        const shortageResp = await http.get<ShortageItemDto[]>(
          `/customer/orders/${order.orderId}/shortages`
        );
        if (shortageResp.data && shortageResp.data.length > 0) {
          // 有缺书：弹出缺书登记对话框，由用户选择处理方案
          setShortages(shortageResp.data);
          setShortageOrder(order);
          setShortageNote('');
          setShortageModalVisible(true);
          setCart([]);
          await loadOrders(orderStatusFilter);
          message.warning(
            `订单已创建（订单号：${order.orderId}），其中部分图书库存不足，请选择缺书处理方式`
          );
        } else {
          // 无缺书：行为与原 JavaFX 一致，提示在“我的订单”中付款
          message.success(
            `订单创建成功（订单号：${order.orderId}），请在“我的订单”中完成付款`
          );
          setCart([]);
          await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
        }
      } catch {
        // 缺书检查接口异常时，退化为无缺书场景提示
        message.success(
          `订单创建成功（订单号：${order.orderId}），请在“我的订单”中完成付款`
        );
        setCart([]);
        await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '下单失败');
    }
  };

  const openProfileModal = () => {
    setProfileModalVisible(true);
  };

  const handleRecharge = async () => {
    if (!customerId) return;
    let amountStr = '';
    const modal = Modal.confirm({
      title: '账户充值',
      content: (
        <div style={{ marginTop: 12 }}>
          <Space direction="vertical" size="small" style={{ width: '100%' }}>
            <Space wrap>
              {[50, 100, 200, 500, 1000].map((v) => (
                <Button key={v} onClick={() => {
                  amountStr = String(v);
                }}>
                  ¥{v}
                </Button>
              ))}
            </Space>
            <Input
              placeholder="自定义金额"
              onChange={(e) => {
                amountStr = e.target.value;
              }}
            />
          </Space>
        </div>
      ),
      okText: '确认充值',
      cancelText: '取消',
      async onOk() {
        try {
          const val = amountStr.trim();
          if (!val) {
            message.warning('请输入充值金额');
            return Promise.reject();
          }
          const amount = Number(val);
          if (!Number.isFinite(amount) || amount <= 0) {
            message.warning('充值金额必须大于0');
            return Promise.reject();
          }
          const resp = await http.post<CustomerSummary>(
            `/customer/${customerId}/recharge`,
            { amount }
          );
          setSummary(resp.data);
          message.success(`充值成功，当前余额：¥${resp.data.accountBalance.toFixed(2)}`);
        } catch (e: any) {
          message.error(e?.response?.data?.message || '充值失败');
          return Promise.reject();
        }
      }
    });
  };

  const handleUpdateProfile = async (values: any) => {
    if (!customerId) return;
    try {
      const resp = await http.put<CustomerSummary>(`/customer/${customerId}/profile`, {
        realName: values.realName,
        mobilePhone: values.mobilePhone,
        email: values.email
      });
      setSummary(resp.data);
      message.success('个人信息已更新');
      setProfileModalVisible(false);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '更新失败');
    }
  };

  const loadNotifications = async () => {
    if (!customerId) return;
    try {
      setLoadingNotifications(true);
      const resp = await http.get<CustomerNotificationDto[]>(
        `/customer/${customerId}/notifications`
      );
      setNotifications(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载通知列表失败');
    } finally {
      setLoadingNotifications(false);
    }
  };

  const openNotifications = async () => {
    setNotificationsVisible(true);
    await loadNotifications();
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          background: '#667eea',
          display: 'flex',
          alignItems: 'center',
          padding: '0 24px'
        }}
      >
        <Title level={4} style={{ color: '#fff', margin: 0 }}>
          网上书店
        </Title>
        <div style={{ flex: 1 }} />
        <Space size="large" align="center">
          <Text style={{ color: '#fff' }}>
            欢迎，{summary?.realName || customerName}
          </Text>
          <Button type="text" style={{ color: '#fff' }} onClick={openProfileModal}>
            修改信息
          </Button>
          <Button type="text" style={{ color: '#fff' }} onClick={openOrders}>
            我的订单
          </Button>
          <Badge dot>
            <Button
              type="text"
              icon={<BellOutlined />}
              style={{ color: '#fff' }}
              onClick={openNotifications}
            >
              通知
            </Button>
          </Badge>
          <Button
            type="text"
            icon={<LogoutOutlined />}
            style={{ color: '#fff' }}
            onClick={logout}
          >
            退出登录
          </Button>
        </Space>
      </Header>

      <Layout>
        <Sider
          width={720}
          style={{
            background: '#fff',
            borderRight: '1px solid #f0f0f0',
            padding: 16
          }}
        >
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={5} style={{ margin: 0 }}>
              书目浏览
            </Title>
            <Space.Compact style={{ width: '100%' }}>
              <Input
                placeholder="输入书名、作者、关键字、书号或出版社搜索..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onPressEnter={searchBooks}
              />
              <Button type="primary" onClick={searchBooks}>
                搜索
              </Button>
              <Button onClick={loadAllBooks}>刷新</Button>
            </Space.Compact>

            <Table<BookDto>
              rowKey="bookId"
              size="middle"
              columns={bookColumns}
              dataSource={books}
              loading={loadingBooks}
              pagination={{ pageSize: 10 }}
              scroll={{ y: 540 }}
            />
          </Space>
        </Sider>

        <Layout>
          <Content style={{ padding: 16, background: '#f5f5f5' }}>
            <Row gutter={[16, 16]}>
              <Col span={24}>
                <Title level={5}>购物车</Title>
                <Table<CartItem>
                  rowKey="bookId"
                  size="small"
                  columns={cartColumns}
                  dataSource={cart}
                  pagination={false}
                  locale={{ emptyText: '暂未加入任何图书' }}
                  style={{ marginBottom: 16, background: '#fff' }}
                />
                <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                  <Space>
                    <Statistic
                      title="应付金额"
                      prefix="¥"
                      value={totalAmount}
                      precision={2}
                      valueStyle={{ color: '#e74c3c', fontSize: 22 }}
                    />
                    <Text type="secondary">（已按您的信用等级自动计算折扣）</Text>
                  </Space>
                  <Space>
                    <Button onClick={clearCart}>清空购物车</Button>
                    <Button
                      type="primary"
                      size="large"
                      disabled={cart.length === 0}
                      onClick={submitOrder}
                    >
                      提交订单
                    </Button>
                  </Space>
                </Space>
              </Col>
            </Row>
          </Content>

          <Footer style={{ background: '#fff', borderTop: '1px solid #f0f0f0' }}>
            <Row gutter={32} align="middle">
              <Col>
                <Statistic
                  title="账户余额"
                  prefix="¥"
                  value={summary ? summary.accountBalance : 0}
                  precision={2}
                />
              </Col>
              <Col>
                <Button type="link" onClick={handleRecharge}>
                  充值
                </Button>
              </Col>
              <Col>
                <Statistic
                  title="信用等级"
                  value={summary ? summary.creditLevelName : '加载中...'}
                  suffix={
                    summary
                      ? `（折扣 ${Math.round(summary.discountRate * 100)}%）`
                      : undefined
                  }
                />
              </Col>
              <Col flex="auto">
                <Text type="secondary">
                  {summary
                    ? summary.privilegeText
                    : '正在加载信用等级权限说明...'}
                </Text>
              </Col>
              <Col>
                <Button type="link" onClick={openAddressModal}>
                  管理地址
                </Button>
              </Col>
            </Row>
          </Footer>
        </Layout>
      </Layout>

      {/* 书目详情弹窗 */}
      <Modal
        open={!!detailBook}
        title={detailBook ? `书目详情 - ${detailBook.title}` : ''}
        footer={null}
        onCancel={() => setDetailBook(null)}
        width={640}
      >
        {detailBook && (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="书号">{detailBook.bookId}</Descriptions.Item>
            <Descriptions.Item label="ISBN">{detailBook.isbn || ''}</Descriptions.Item>
            <Descriptions.Item label="书名" span={2}>
              {detailBook.title}
            </Descriptions.Item>
            <Descriptions.Item label="出版社" span={2}>
              {detailBook.publisher || ''}
            </Descriptions.Item>
            <Descriptions.Item label="出版日期">
              {detailBook.publishDate || ''}
            </Descriptions.Item>
            <Descriptions.Item label="版次">{detailBook.edition || ''}</Descriptions.Item>
            <Descriptions.Item label="定价">
              {detailBook.price != null ? `¥${detailBook.price.toFixed(2)}` : ''}
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              {detailBook.status ? <Tag>{detailBook.status}</Tag> : null}
            </Descriptions.Item>
            <Descriptions.Item label="目录 / 简介" span={2}>
              <div style={{ whiteSpace: 'pre-wrap', maxHeight: 260, overflowY: 'auto' }}>
                {detailBook.catalog || ''}
              </div>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Modal>

      {/* 消息通知列表弹窗（对应 JavaFX showOutOfStockNotificationList） */}
      <Modal
        open={notificationsVisible}
        title="消息通知"
        footer={null}
        width={760}
        onCancel={() => setNotificationsVisible(false)}
      >
        <Table<CustomerNotificationDto>
          rowKey="notificationId"
          size="small"
          loading={loadingNotifications}
          dataSource={notifications}
          pagination={{ pageSize: 10 }}
          columns={[
            {
              title: '类型',
              dataIndex: 'type',
              width: 120
            },
            {
              title: '订单号',
              dataIndex: 'orderId',
              width: 120
            },
            {
              title: '标题',
              dataIndex: 'title',
              width: 180
            },
            {
              title: '内容',
              dataIndex: 'content',
              width: 260
            },
            {
              title: '时间',
              dataIndex: 'createdTime',
              width: 160
            }
          ]}
        />
      </Modal>

      {/* 缺书登记决策弹窗（对应 JavaFX showOutOfStockRequestDialog） */}
      <Modal
        open={shortageModalVisible}
        title="缺书登记"
        onCancel={() => setShortageModalVisible(false)}
        footer={null}
        width={780}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Text>
            订单号：{shortageOrder?.orderId}，以下图书当前库存不足，请选择处理方式：
          </Text>
          <Table<ShortageItemDto>
            rowKey="orderItemId"
            size="small"
            pagination={false}
            dataSource={shortages}
            columns={[
              { title: '书号', dataIndex: 'bookId', width: 160 },
              { title: '订购数量', dataIndex: 'quantity', width: 120 },
              { title: '当前库存', dataIndex: 'currentStock', width: 120 }
            ]}
          />
          <div>
            <Text strong>额外请求备注（可选）：</Text>
            <Input.TextArea
              style={{ marginTop: 4 }}
              rows={3}
              placeholder="例如：希望本书到货后第一时间通知我..."
              value={shortageNote}
              onChange={(e) => setShortageNote(e.target.value)}
            />
          </div>
          <Space style={{ justifyContent: 'flex-end', width: '100%' }}>
            <Button onClick={() => setShortageModalVisible(false)}>取消</Button>
            <Button
              onClick={async () => {
                if (!shortageOrder) return;
                try {
                  await http.post(
                    `/customer/orders/${shortageOrder.orderId}/shortages/decision`,
                    {
                      decision: 'REQUEST_ONLY',
                      customerNote: shortageNote || undefined
                    }
                  );
                  message.success(
                    '缺书登记已提交，订单状态已标记为【缺货待确认】，请留意通知'
                  );
                  setShortageModalVisible(false);
                  await loadOrders(orderStatusFilter);
                } catch (e: any) {
                  message.error(e?.response?.data?.message || '提交缺书登记失败');
                }
              }}
            >
              仅提交缺书登记（暂不付款）
            </Button>
            <Button
              type="primary"
              onClick={async () => {
                if (!shortageOrder) return;
                try {
                  await http.post(
                    `/customer/orders/${shortageOrder.orderId}/shortages/decision`,
                    {
                      decision: 'PAY_AND_CREATE',
                      customerNote: shortageNote || undefined
                    }
                  );
                  await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
                  message.success('缺书登记已生成并完成付款，等待发货');
                  setShortageModalVisible(false);
                } catch (e: any) {
                  message.error(e?.response?.data?.message || '付款或缺书登记生成失败');
                }
              }}
            >
              付款并生成缺书记录
            </Button>
          </Space>
        </Space>
      </Modal>

      {/* 我的订单弹窗（对应 JavaFX showMyOrders） */}
      <Modal
        open={ordersVisible}
        title="我的订单"
        width={820}
        footer={null}
        onCancel={() => {
          setOrdersVisible(false);
          setActiveOrderDetail(null);
        }}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Space>
            <span>订单状态：</span>
            <Select
              style={{ width: 260 }}
              value={orderStatusFilter}
              onChange={(val) => {
                setOrderStatusFilter(val);
                loadOrders(val);
              }}
              options={[
                { label: '全部', value: '全部' },
                { label: '待付款 PENDING_PAYMENT', value: 'PENDING_PAYMENT' },
                { label: '缺货待确认 OUT_OF_STOCK_PENDING', value: 'OUT_OF_STOCK_PENDING' },
                { label: '待发货 PENDING_SHIPMENT', value: 'PENDING_SHIPMENT' },
                { label: '配送中 DELIVERING', value: 'DELIVERING' },
                { label: '已发货 SHIPPED', value: 'SHIPPED' },
                { label: '已完成 COMPLETED', value: 'COMPLETED' },
                { label: '已取消 CANCELLED', value: 'CANCELLED' }
              ]}
            />
          </Space>

          <Table<SalesOrderDto>
            rowKey="orderId"
            size="small"
            columns={orderColumns}
            dataSource={orders}
            loading={loadingOrders}
            pagination={{ pageSize: 8 }}
          />

          {activeOrderDetail && (
            <div style={{ marginTop: 8 }}>
              <Title level={5}>订单详情 - {activeOrderDetail.order.orderId}</Title>
              <Text>
                状态：{activeOrderDetail.order.orderStatus}，下单时间：
                {activeOrderDetail.order.orderTime}，应付金额：¥
                {activeOrderDetail.order.payableAmount.toFixed(2)}
              </Text>

              <Title level={5} style={{ marginTop: 12 }}>
                订单明细
              </Title>
              <Table<OrderDetailItem>
                rowKey="orderItemId"
                size="small"
                columns={[
                  { title: '书号', dataIndex: 'bookId' },
                  { title: '订购数量', dataIndex: 'quantity' },
                  {
                    title: '成交单价',
                    dataIndex: 'unitPrice',
                    render: (v: number) => `¥${v.toFixed(2)}`
                  },
                  {
                    title: '小计',
                    dataIndex: 'subAmount',
                    render: (v: number) => `¥${v.toFixed(2)}`
                  },
                  {
                    title: '已发货',
                    dataIndex: 'shippedQuantity',
                    render: (_: number | null, r) => r.shippedQuantity ?? 0
                  },
                  {
                    title: '已收货',
                    dataIndex: 'receivedQuantity',
                    render: (_: number | null, r) => r.receivedQuantity ?? 0
                  }
                ]}
                dataSource={activeOrderDetail.items}
                pagination={false}
              />

              <Title level={5} style={{ marginTop: 12 }}>
                发货信息
              </Title>
              <Table<ShipmentDto>
                rowKey="shipmentId"
                size="small"
                columns={[
                  { title: '发货单号', dataIndex: 'shipmentId' },
                  { title: '快递公司', dataIndex: 'carrier' },
                  { title: '快递单号', dataIndex: 'trackingNumber' },
                  { title: '发货时间', dataIndex: 'shipTime' }
                ]}
                dataSource={activeOrderDetail.shipments}
                pagination={false}
              />
            </div>
          )}
        </Space>
      </Modal>

      {/* 订单收货确认弹窗（对应 JavaFX showReceiveDialog） */}
      <Modal
        open={receiveModalVisible}
        title="确认收货"
        onCancel={() => setReceiveModalVisible(false)}
        onOk={handleConfirmReceive}
        okText="确认收货"
        cancelText="取消"
        width={720}
      >
        {activeOrderDetail ? (
          <Table<OrderDetailItem>
            rowKey="orderItemId"
            size="small"
            dataSource={activeOrderDetail.items.filter((it) => {
              const shipped = it.shippedQuantity ?? 0;
              const received = it.receivedQuantity ?? 0;
              return shipped - received > 0;
            })}
            pagination={false}
            columns={[
              { title: '书号', dataIndex: 'bookId', width: 120 },
              { title: '订购数量', dataIndex: 'quantity', width: 90 },
              {
                title: '已发货',
                dataIndex: 'shippedQuantity',
                width: 90,
                render: (_: any, r) => r.shippedQuantity ?? 0
              },
              {
                title: '已收货',
                dataIndex: 'receivedQuantity',
                width: 90,
                render: (_: any, r) => r.receivedQuantity ?? 0
              },
              {
                title: '本次确认收货数量',
                key: 'receiveQty',
                render: (_: any, r) => {
                  const shipped = r.shippedQuantity ?? 0;
                  const received = r.receivedQuantity ?? 0;
                  const remain = shipped - received;
                  return (
                    <InputNumber
                      min={0}
                      max={remain}
                      value={receiveQuantities[r.orderItemId] ?? remain}
                      onChange={(val) => {
                        setReceiveQuantities((prev) => ({
                          ...prev,
                          [r.orderItemId]: (val ?? 0) as number
                        }));
                      }}
                    />
                  );
                }
              }
            ]}
          />
        ) : (
          <Text type="secondary">请先选择一个订单后再进行收货确认。</Text>
        )}
      </Modal>

      {/* 收货地址管理弹窗（对应 JavaFX showAddressManagement） */}
      <Modal
        open={addressModalVisible}
        title="收货地址管理"
        width={720}
        footer={null}
        onCancel={() => setAddressModalVisible(false)}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Table<CustomerAddressDto>
            rowKey="addressId"
            size="small"
            dataSource={addresses}
            loading={loadingAddresses}
            pagination={false}
            columns={[
              { title: '收件人', dataIndex: 'receiver', width: 100 },
              { title: '电话', dataIndex: 'phone', width: 120 },
              {
                title: '地址',
                dataIndex: 'detail',
                render: (_, r) =>
                  `${r.province || ''}${r.city || ''}${r.district || ''}${r.detail || ''}`
              },
              {
                title: '默认',
                dataIndex: 'isDefault',
                width: 80,
                render: (v: boolean) => (v ? <Tag color="green">是</Tag> : null)
              },
              {
                title: '操作',
                key: 'action',
                width: 160,
                render: (_, r) => (
                  <Space>
                    <Button
                      size="small"
                      type="link"
                      disabled={r.isDefault}
                      onClick={() => setDefaultAddress(r.addressId)}
                    >
                      设为默认
                    </Button>
                    <Popconfirm
                      title="确认删除该地址？"
                      onConfirm={() => deleteAddress(r.addressId)}
                    >
                      <Button size="small" type="link" danger>
                        删除
                      </Button>
                    </Popconfirm>
                  </Space>
                )
              }
            ]}
          />

          <Button type="dashed" onClick={() => setAddressFormVisible(true)}>
            新增地址
          </Button>
        </Space>
      </Modal>

      {/* 新增地址表单弹窗（对应 showAddAddressDialog） */}
      <Modal
        open={addressFormVisible}
        title="新增收货地址"
        onCancel={() => setAddressFormVisible(false)}
        onOk={() => {
          const form = document.getElementById('address-form') as HTMLFormElement | null;
          if (form) {
            form.requestSubmit();
          }
        }}
        okText="保存"
        cancelText="取消"
      >
        <Form
          id="address-form"
          layout="vertical"
          onFinish={handleAddAddress}
          initialValues={{ isDefault: false }}
        >
          <Form.Item
            label="收件人"
            name="receiver"
            rules={[{ required: true, message: '请输入收件人' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item label="电话" name="phone">
            <Input />
          </Form.Item>
          <Form.Item label="省" name="province">
            <Input />
          </Form.Item>
          <Form.Item label="市" name="city">
            <Input />
          </Form.Item>
          <Form.Item label="区/县" name="district">
            <Input />
          </Form.Item>
          <Form.Item
            label="详细地址"
            name="detail"
            rules={[{ required: true, message: '请输入详细地址' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="isDefault" valuePropName="checked">
            <Checkbox>设为默认地址</Checkbox>
          </Form.Item>
        </Form>
      </Modal>

      {/* 修改个人信息弹窗（对应 showEditCustomerInfoDialog） */}
      <Modal
        open={profileModalVisible}
        title="修改个人信息"
        onCancel={() => setProfileModalVisible(false)}
        okText="保存"
        cancelText="取消"
        onOk={() => {
          const form = document.getElementById('profile-form') as HTMLFormElement | null;
          if (form) {
            form.requestSubmit();
          }
        }}
      >
        <Form
          id="profile-form"
          layout="vertical"
          initialValues={{
            username: summary?.username,
            realName: summary?.realName,
            mobilePhone: summary?.mobilePhone,
            email: summary?.email
          }}
          onFinish={handleUpdateProfile}
        >
          <Form.Item label="用户名">
            <Input value={summary?.username} disabled />
          </Form.Item>
          <Form.Item
            label="真实姓名"
            name="realName"
            rules={[{ required: true, message: '真实姓名不能为空' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item label="手机号" name="mobilePhone">
            <Input />
          </Form.Item>
          <Form.Item
            label="邮箱"
            name="email"
            rules={[
              {
                type: 'email',
                message: '邮箱格式不正确'
              }
            ]}
          >
            <Input />
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default CustomerLayout;

