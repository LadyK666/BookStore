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
  InputNumber,
  Card,
  List,
  Drawer,
  Empty,
  Tabs,
  theme,
  Radio,
  Segmented
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  ShoppingCartOutlined,
  LogoutOutlined,
  BellOutlined,
  SearchOutlined,
  UserOutlined,
  BookOutlined,
  EnvironmentOutlined,
  HomeOutlined,
  DeleteOutlined,
  CloseOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons';
import { useLocation, useNavigate } from 'react-router-dom';
import { http } from '../../api/http';

const { Header, Content } = Layout;
const { Title, Text } = Typography;
const { useToken } = theme;

// --- Interfaces ---
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
  // 丛书字段
  seriesFlag?: boolean;
  parentBookId?: string | null;
}

interface AuthorDto {
  authorId: number;
  authorName: string;
  nationality?: string;
  biography?: string;
}

interface KeywordDto {
  keywordId: number;
  keywordText: string;
}

interface BookDetailResp {
  book: BookDto;
  authors: AuthorDto[];
  keywords: KeywordDto[];
  childBooks?: BookDto[];  // 丛书的子书目列表
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
  shippingAddressSnapshot?: string;
  paymentTime?: string | null;
  hasShipments?: boolean; // 是否有发货记录
}

interface OrderWithShipmentFlag {
  order: SalesOrderDto;
  hasShipments: boolean;
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
  shipmentStatus?: string; // SHIPPED, DELIVERED, IN_TRANSIT
}

interface ShipmentItemDto {
  orderItemId: number;
  bookId: string;
  shipQuantity: number;
}

interface ShipmentWithItems {
  shipment: ShipmentDto;
  items: ShipmentItemDto[];
}

interface OrderDetailResp {
  order: SalesOrderDto;
  items: OrderDetailItem[];
  shipments: ShipmentDto[]; // 向后兼容
  shipmentsWithItems?: ShipmentWithItems[]; // 新字段，包含items信息
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
  const { token } = useToken();
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state || {}) as Partial<LocationState>;
  const customerId = state.customerId;
  const customerName = state.customerName || '顾客';

  // --- States ---
  const [books, setBooks] = useState<BookDto[]>([]);
  const [loadingBooks, setLoadingBooks] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [cart, setCart] = useState<CartItem[]>([]);
  const [detailBook, setDetailBook] = useState<BookDetailResp | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [summary, setSummary] = useState<CustomerSummary | null>(null);
  // Orders
  const [ordersVisible, setOrdersVisible] = useState(false);
  const [orders, setOrders] = useState<SalesOrderDto[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [orderStatusFilter, setOrderStatusFilter] = useState<string>('全部');
  const [activeOrderDetail, setActiveOrderDetail] = useState<OrderDetailResp | null>(null);
  const [orderDetailVisible, setOrderDetailVisible] = useState(false);
  // Receive
  const [receiveModalVisible, setReceiveModalVisible] = useState(false);
  const [receiveQuantities, setReceiveQuantities] = useState<Record<number, number>>({});
  const [selectedShipmentId, setSelectedShipmentId] = useState<number | null>(null);
  // Address
  const [addressModalVisible, setAddressModalVisible] = useState(false);
  const [addresses, setAddresses] = useState<CustomerAddressDto[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState(false);
  const [addressFormVisible, setAddressFormVisible] = useState(false);
  // Profile
  const [profileModalVisible, setProfileModalVisible] = useState(false);
  // Notifications
  const [notificationsVisible, setNotificationsVisible] = useState(false);
  const [notificationTab, setNotificationTab] = useState<'messages' | 'inquiries'>('messages');
  const [notifications, setNotifications] = useState<CustomerNotificationDto[]>([]);
  const [loadingNotifications, setLoadingNotifications] = useState(false);
  // Shortage
  const [shortageModalVisible, setShortageModalVisible] = useState(false);
  const [shortages, setShortages] = useState<ShortageItemDto[]>([]);
  const [shortageNote, setShortageNote] = useState('');
  const [shortageOrder, setShortageOrder] = useState<SalesOrderDto | null>(null);
  const [shortageDecision, setShortageDecision] = useState<'pay' | 'request_only' | 'cancel'>('pay');

  const [cartVisible, setCartVisible] = useState(false);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);

  // Inquiry
  const [inquiryModalVisible, setInquiryModalVisible] = useState(false);
  const [inquiryForm] = Form.useForm();

  // My Inquiries List
  const [myInquiryDrawerVisible, setMyInquiryDrawerVisible] = useState(false);
  const [myInquiries, setMyInquiries] = useState<any[]>([]);
  const [loadingMyInquiries, setLoadingMyInquiries] = useState(false);

  // Advanced Search
  const [advSearchVisible, setAdvSearchVisible] = useState(false);
  const [advSearchType, setAdvSearchType] = useState<'author' | 'keywords'>('author');
  const [advAuthorName, setAdvAuthorName] = useState('');
  const [advAuthorOrder, setAdvAuthorOrder] = useState<number>(0);
  const [advKeywords, setAdvKeywords] = useState('');
  const [advMinMatch, setAdvMinMatch] = useState<number>(1);
  const [advSearchLoading, setAdvSearchLoading] = useState(false);
  const [isAdvancedSearchResult, setIsAdvancedSearchResult] = useState(false);

  const loadMyInquiries = async () => {
    if (!customerId) return;
    setLoadingMyInquiries(true);
    try {
      const resp = await http.get(`/customer/${customerId}/inquiries`);
      setMyInquiries(resp.data);
    } catch (e: any) {
      message.error('无法加载询价记录');
    } finally {
      setLoadingMyInquiries(false);
    }
  };

  const advancedSearch = async () => {
    setAdvSearchLoading(true);
    try {
      if (advSearchType === 'author') {
        if (!advAuthorName.trim()) {
          message.warning('请输入作者名');
          return;
        }
        const resp = await http.get<BookDto[]>('/customer/books/search/by-author', {
          params: { author: advAuthorName.trim(), authorOrder: advAuthorOrder }
        });
        setBooks(resp.data);
        setIsAdvancedSearchResult(true);
        message.success(`找到 ${resp.data.length} 本书`);
      } else {
        if (!advKeywords.trim()) {
          message.warning('请输入关键字');
          return;
        }
        const resp = await http.get<any[]>('/customer/books/search/by-keywords', {
          params: { keywords: advKeywords.trim(), minMatch: advMinMatch }
        });
        // 从返回结果中提取 Book 对象
        const booksResult = resp.data.map((item: any) => ({
          ...item.book,
          matchCount: item.matchCount,
          totalKeywords: item.totalKeywords
        }));
        setBooks(booksResult);
        setIsAdvancedSearchResult(true);
        message.success(`找到 ${booksResult.length} 本书`);
      }
      setAdvSearchVisible(false);
    } catch (e: any) {
      message.error('搜索失败');
    } finally {
      setAdvSearchLoading(false);
    }
  };

  const resetToInitialView = async () => {
    setIsAdvancedSearchResult(false);
    setKeyword('');
    await loadAllBooks();
  };

  // When cart opens, load addresses and set default
  const openCart = async () => {
    setCartVisible(true);
    await loadAddresses();
  };

  // --- Cart API Operations ---
  const loadCart = async () => {
    if (!customerId) return;
    try {
      const resp = await http.get<CartItem[]>(`/customer/${customerId}/cart`);
      setCart(resp.data);
    } catch (e: any) {
      console.error('加载购物车失败', e);
    }
  };

  const addToCartApi = async (bookId: string, title: string, quantity: number, unitPrice: number) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/cart`, { bookId, quantity });
      // 重新加载购物车以获取最新状态
      await loadCart();
    } catch (e: any) {
      message.error('添加购物车失败');
    }
  };

  const removeFromCartApi = async (bookId: string) => {
    if (!customerId) return;
    try {
      await http.delete(`/customer/${customerId}/cart/${bookId}`);
      await loadCart();
    } catch (e: any) {
      message.error('移除失败');
    }
  };

  const updateCartQuantityApi = async (bookId: string, quantity: number) => {
    if (!customerId) return;
    if (quantity <= 0) {
      await removeFromCartApi(bookId);
      return;
    }
    try {
      await http.put(`/customer/${customerId}/cart/${bookId}`, { quantity });
      await loadCart();
    } catch (e: any) {
      message.error('更新数量失败');
    }
  };

  const clearCartApi = async () => {
    if (!customerId) return;
    try {
      await http.delete(`/customer/${customerId}/cart`);
      setCart([]);
    } catch (e: any) {
      console.error('清空购物车失败', e);
    }
  };

  // --- Logic ---
  useEffect(() => {
    if (!customerId) {
      if (!location.pathname.includes('/login')) {
        navigate('/login');
      }
      return;
    }
    loadSummary();
    loadAllBooks();
    loadAddresses();
    loadNotifications();
    loadCart();
  }, [customerId]);

  // Auto-search with debounce
  useEffect(() => {
    if (!customerId) return;
    const timer = setTimeout(() => {
      if (keyword.trim()) {
        searchBooks(keyword);
        setIsAdvancedSearchResult(false); // 普通搜索时重置高级搜索状态
      } else {
        loadAllBooks();
        setIsAdvancedSearchResult(false); // 清空搜索时重置高级搜索状态
      }
    }, 300);
    return () => clearTimeout(timer);
  }, [keyword]);

  const loadSummary = async () => {
    try {
      const resp = await http.get<CustomerSummary>(`/customer/${customerId}/summary`);
      setSummary(resp.data);
    } catch (e: any) { }
  };

  const loadOrders = async (status: string) => {
    if (!customerId) return;
    try {
      setLoadingOrders(true);
      const resp = await http.get<OrderWithShipmentFlag[]>(`/customer/${customerId}/orders`, { params: { status: status === '全部' ? undefined : status } });
      // 将 OrderWithShipmentFlag 转换为 SalesOrderDto，并添加 hasShipments 字段
      const ordersWithFlag = resp.data.map(item => ({
        ...item.order,
        hasShipments: item.hasShipments
      }));
      setOrders(ordersWithFlag);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单失败');
    } finally {
      setLoadingOrders(false);
    }
  };

  const loadBookDetail = async (bookId: string) => {
    try {
      setLoadingDetail(true);
      const resp = await http.get<BookDetailResp>(`/customer/books/${bookId}`);
      setDetailBook(resp.data);
    } catch (e: any) {
      message.error('加载书籍详情失败');
    } finally {
      setLoadingDetail(false);
    }
  };

  const openReceiveModal = async (orderId: number) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/customer/orders/${orderId}`);
      setActiveOrderDetail(resp.data);
      setSelectedShipmentId(null); // 重置选择
      
      // 检查是否有可收货的shipment
      const shipmentsWithItems = resp.data.shipmentsWithItems || [];
      const shippableShipments = shipmentsWithItems.filter(swi => swi.shipment.shipmentStatus === 'SHIPPED');
      if (shippableShipments.length === 0) {
        message.info('当前暂无可收货的发货单');
        return;
      }
      
      setReceiveModalVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单详情失败');
    }
  };

  const handleConfirmReceive = async () => {
    if (!activeOrderDetail) return;
    
    // 检查是否是分次发货
    const shipments = activeOrderDetail.shipments || [];
    const isPartialShipment = shipments.length > 1;
    
    if (isPartialShipment) {
      // 分次发货：必须选择shipment
      if (!selectedShipmentId) {
        message.warning('请选择要收货的发货单');
        return;
      }
      try {
        await http.post(`/customer/orders/${activeOrderDetail.order.orderId}/receive`, { shipmentId: selectedShipmentId });
        message.success('确认收货成功');
        setReceiveModalVisible(false);
        setSelectedShipmentId(null);
        await loadOrders(orderStatusFilter);
      } catch (e: any) {
        message.error(e?.response?.data?.message || '确认收货失败');
      }
    } else {
      // 整体发货：直接确认收货全部（使用shipmentId）
      if (shipments.length === 1) {
        try {
          await http.post(`/customer/orders/${activeOrderDetail.order.orderId}/receive`, { shipmentId: shipments[0].shipmentId });
          message.success('确认收货成功');
          setReceiveModalVisible(false);
          setSelectedShipmentId(null);
          await loadOrders(orderStatusFilter);
        } catch (e: any) {
          message.error(e?.response?.data?.message || '确认收货失败');
        }
      }
    }
  };

  const loadAddresses = async () => {
    if (!customerId) return;
    try {
      setLoadingAddresses(true);
      const resp = await http.get<any[]>(`/customer/${customerId}/addresses`);
      // Map backend 'default' field to frontend 'isDefault'
      const mapped: CustomerAddressDto[] = resp.data.map((a: any) => ({
        ...a,
        isDefault: a.default ?? a.isDefault ?? false
      }));
      setAddresses(mapped);
    } catch (e: any) { } finally {
      setLoadingAddresses(false);
    }
  };

  const loadAllBooks = async () => {
    try {
      setLoadingBooks(true);
      const resp = await http.get<BookDto[]>('/customer/books');
      setBooks(resp.data);
    } catch (e: any) { } finally {
      setLoadingBooks(false);
    }
  };

  const submitInquiry = async (values: any) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/inquiries`, values);
      message.success('询价请求已提交，我们会尽快回复');
      setInquiryModalVisible(false);
      inquiryForm.resetFields();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '提交失败');
    }
  };

  const searchBooks = async (searchKeyword?: string) => {
    const kw = (searchKeyword ?? keyword).trim();
    if (!kw) { loadAllBooks(); return; }
    try {
      setLoadingBooks(true);
      const resp = await http.get<BookDto[]>('/customer/books/search', { params: { keyword: kw } });
      setBooks(resp.data);
    } catch (e: any) { } finally {
      setLoadingBooks(false);
    }
  };

  const addToCart = (book: BookDto) => {
    let qty = 1;
    Modal.confirm({
      title: '加入购物车',
      icon: <ShoppingCartOutlined />,
      content: (
        <div style={{ marginTop: 12 }}>
          <p>{book.title}</p>
          <InputNumber min={1} defaultValue={1} onChange={v => qty = Number(v)} addonAfter="本" style={{ width: '100%' }} />
          <div style={{ marginTop: 8, color: '#888' }}>单价: ¥{book.price.toFixed(2)}</div>
        </div>
      ),
      onOk: async () => {
        if (qty <= 0) return;
        await addToCartApi(book.bookId, book.title, qty, Number(book.price));
        message.success(`已添加`);
        setCartVisible(true);
      }
    });
  };

  const removeFromCart = async (bookId: string) => {
    await removeFromCartApi(bookId);
  };

  const totalAmount = useMemo(() => {
    const discount = summary?.discountRate ?? 1;
    return cart.reduce((sum, item) => sum + item.quantity * item.unitPrice * discount, 0);
  }, [cart, summary]);

  const payOrder = async (orderId: number) => {
    try {
      await http.post(`/customer/orders/${orderId}/pay`);
      message.success('付款成功');
      await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
      if (activeOrderDetail?.order.orderId === orderId) {
        openOrderDetail(orderId);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '付款失败');
    }
  };

  const cancelOrder = async (orderId: number) => {
    try {
      await http.post(`/customer/orders/${orderId}/cancel`);
      message.success('订单已取消');
      await loadOrders(orderStatusFilter);
      if (activeOrderDetail?.order.orderId === orderId) {
        openOrderDetail(orderId);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '取消失败');
    }
  };

  const handleAddAddress = async (values: any) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/addresses`, {
        ...values,
        isDefault: values.isDefault || false
      });
      message.success('地址已添加');
      setAddressFormVisible(false);
      await loadAddresses();
    } catch (e: any) {
      message.error('新增地址失败');
    }
  };

  const setDefaultAddress = async (addressId: number) => {
    if (!customerId) return;
    try {
      await http.post(`/customer/${customerId}/addresses/${addressId}/default`);
      message.success('已设为默认地址');
      await loadAddresses();
    } catch (e: any) { message.error('失败'); }
  };

  const deleteAddress = async (addressId: number) => {
    try {
      await http.delete(`/customer/${customerId}/addresses/${addressId}`);
      message.success('地址已删除');
      await loadAddresses();
    } catch (e: any) { message.error('失败'); }
  };

  const submitOrder = async () => {
    if (cart.length === 0) { message.warning('购物车为空'); return; }
    try {
      // 准备收货地址快照
      let snapshot = '';
      if (addresses.length > 0) {
        const selectedAddr = selectedAddressId
          ? addresses.find((a) => a.addressId === selectedAddressId)
          : (addresses.find((a) => a.isDefault) || addresses[0]);
        if (selectedAddr) {
          snapshot = `${selectedAddr.city || ''} ${selectedAddr.detail}`;
          if (selectedAddr.receiver) snapshot = `${selectedAddr.receiver} ${snapshot}`;
        }
      } else if (summary) {
        snapshot = summary.realName || summary.username;
      }

      const orderPayload = {
        items: cart.map((c) => ({ bookId: c.bookId, quantity: c.quantity, unitPrice: c.unitPrice })),
        shippingAddressSnapshot: snapshot
      };

      // 先检查库存
      const checkResp = await http.post<ShortageItemDto[]>(`/customer/${customerId}/orders/check-stock`, orderPayload);

      if (checkResp.data && checkResp.data.length > 0) {
        // 有缺货商品，弹出缺书登记窗口，此时不创建订单
        setShortages(checkResp.data);
        setShortageOrder(null); // 订单尚未创建
        setShortageModalVisible(true);
        // 保存订单请求参数供后续使用
        (window as any).__pendingOrderPayload = orderPayload;
      } else {
        // 没有缺货，直接创建订单
        const resp = await http.post<SalesOrderDto>(`/customer/${customerId}/orders`, orderPayload);
        message.success(`下单成功 (${resp.data.orderId})`);
        await clearCartApi();
        setCartVisible(false);
        await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '下单失败');
    }
  };

  // --- Shortage Handling ---
  const handleShortageDecision = async () => {
    if (!shortageOrder) return;
    try {
      // cancel: 直接关闭弹窗（取消），保留订单
      if (shortageDecision === 'cancel') {
        // 直接关闭弹窗，订单保持待付款状态
        message.info('已关闭，订单保持原状，您可稍后在【我的订单】中处理');
      } else {
        // pay: 付款并生成缺书记录 (PAY_AND_CREATE)
        // request_only: 仅提交缺书登记，暂不付款 (REQUEST_ONLY)
        await http.post(`/customer/orders/${shortageOrder.orderId}/shortages/decision`, {
          decision: shortageDecision === 'pay' ? 'PAY_AND_CREATE' : 'REQUEST_ONLY',
          customerNote: shortageNote
        });
        if (shortageDecision === 'pay') {
          message.success('缺书登记已提交并已付款，等待到货后发货');
        } else {
          message.success('缺书登记已提交（暂未付款），等待管理员审核');
        }
        // 处理完毕后清空购物车
        await clearCartApi();
      }
      setShortageModalVisible(false);
      setShortageNote('');
      setCartVisible(false);
      await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '处理失败');
    }
  };

  const handleRecharge = async () => {
    let amt = 0;
    Modal.confirm({
      title: '账户充值',
      content: <InputNumber min={1} addonAfter="元" onChange={v => amt = Number(v)} style={{ width: '100%' }} />,
      onOk: async () => {
        if (amt > 0) {
          await http.post(`/customer/${customerId}/recharge`, { amount: amt });
          message.success('充值成功');
          loadSummary();
        }
      }
    });
  };

  const handleUpdateProfile = async (values: any) => {
    try {
      const resp = await http.put<CustomerSummary>(`/customer/${customerId}/profile`, values);
      setSummary(resp.data);
      message.success('信息更新成功');
      setProfileModalVisible(false);
    } catch (e: any) { message.error('更新失败'); }
  };

  const openOrderDetail = async (orderId: number) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/customer/orders/${orderId}`);
      setActiveOrderDetail(resp.data);
      setOrderDetailVisible(true);
    } catch (e: any) { }
  };

  // --- Notification Local Storage Logic ---
  const getReadNotificationIds = (): number[] => {
    if (!customerId) return [];
    try {
      const stored = localStorage.getItem(`read_notifications_${customerId}`);
      return stored ? JSON.parse(stored) : [];
    } catch { return []; }
  };

  const getDeletedNotificationIds = (): number[] => {
    if (!customerId) return [];
    try {
      const stored = localStorage.getItem(`deleted_notifications_${customerId}`);
      return stored ? JSON.parse(stored) : [];
    } catch { return []; }
  };

  const saveReadNotificationId = (id: number) => {
    if (!customerId) return;
    const current = getReadNotificationIds();
    if (!current.includes(id)) {
      localStorage.setItem(`read_notifications_${customerId}`, JSON.stringify([...current, id]));
    }
  };

  const saveDeletedNotificationId = (id: number) => {
    if (!customerId) return;
    const current = getDeletedNotificationIds();
    if (!current.includes(id)) {
      localStorage.setItem(`deleted_notifications_${customerId}`, JSON.stringify([...current, id]));
    }
  };

  const saveAllReadNotificationIds = (ids: number[]) => {
    if (!customerId) return;
    const current = getReadNotificationIds();
    const newIds = Array.from(new Set([...current, ...ids]));
    localStorage.setItem(`read_notifications_${customerId}`, JSON.stringify(newIds));
  };

  const loadNotifications = async () => {
    try {
      setLoadingNotifications(true);
      const resp = await http.get<CustomerNotificationDto[]>(`/customer/${customerId}/notifications`);
      setNotifications(resp.data);
    } finally { setLoadingNotifications(false); }
  };

  const markRead = async (id: number) => {
    try {
      await http.post(`/customer/${customerId}/notifications/${id}/read`);
      setNotifications(prev => prev.map(n => n.notificationId === id ? { ...n, readFlag: true } : n));
    } catch (e: any) {
      message.error('标记已读失败');
    }
  };

  const markAllRead = async () => {
    const unread = notifications.filter(n => !n.readFlag);
    if (unread.length === 0) return;
    try {
      await http.post(`/customer/${customerId}/notifications/read-all`);
      setNotifications(prev => prev.map(n => ({ ...n, readFlag: true })));
      message.success('全部已读');
    } catch (e: any) {
      message.error('标记全部已读失败');
    }
  };

  const deleteNotification = async (id: number) => {
    try {
      await http.delete(`/customer/${customerId}/notifications/${id}`);
      setNotifications(prev => prev.filter(n => n.notificationId !== id));
      message.success('已删除');
    } catch (e: any) {
      message.error('删除失败');
    }
  };

  const clearAllNotifications = async () => {
    try {
      await http.delete(`/customer/${customerId}/notifications`);
      setNotifications([]);
      message.success('已清除所有通知');
    } catch (e: any) {
      message.error('清除失败');
    }
  };

  const renderMyInquiryItem = (item: any) => {
    const map: Record<string, any> = { 'PENDING': { text: '待处理', color: 'orange' }, 'QUOTED': { text: '已报价', color: 'green' }, 'REJECTED': { text: '已拒绝', color: 'red' }, 'ACCEPTED': { text: '已接受', color: 'blue' } };
    const statusCfg = map[item.status] || { text: item.status, color: 'default' };
    return (
      <List.Item>
        <Card style={{ width: '100%' }} size="small" title={item.bookTitle} extra={<Tag color={statusCfg.color}>{statusCfg.text}</Tag>}>
          <Descriptions column={1} size="small">
            <Descriptions.Item label="需求数量">{item.quantity}</Descriptions.Item>
            <Descriptions.Item label="提交时间">{item.inquiryTime}</Descriptions.Item>
            {item.quotedPrice && <Descriptions.Item label="报价金额" contentStyle={{ color: 'red', fontWeight: 'bold', fontSize: 16 }}>¥{item.quotedPrice}</Descriptions.Item>}
            {item.adminReply && <Descriptions.Item label="管理员回复">{item.adminReply}</Descriptions.Item>}
          </Descriptions>
        </Card>
      </List.Item>
    );
  };

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f7fa' }}>
      <Header style={{
        background: 'rgba(255,255,255,0.7)', backdropFilter: 'blur(20px)', position: 'sticky', top: 0, zIndex: 100,
        display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 24px', boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
      }}>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          <div style={{ width: 32, height: 32, background: token.colorPrimary, borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <UserOutlined style={{ color: '#fff' }} />
          </div>
          <Title level={4} style={{ margin: 0 }}>网上书店</Title>
        </div>
        <Space size="large">
          <Input prefix={<SearchOutlined style={{ color: '#ccc' }} />} placeholder="搜好书..."
            style={{ width: 260, borderRadius: 20 }} value={keyword} onChange={e => setKeyword(e.target.value)} variant="filled" />
          <Button type="link" size="small" onClick={() => setAdvSearchVisible(true)}>高级搜索</Button>

          <Badge count={cart.length}>
            <Button type="text" icon={<ShoppingCartOutlined style={{ fontSize: 18 }} />} onClick={openCart}>购物车</Button>
          </Badge>

          <Button type="text" onClick={() => { setOrdersVisible(true); loadOrders('全部'); }}>我的订单</Button>

          <Badge dot={notifications.some(n => !n.readFlag)}>
            <Button type="text" icon={<BellOutlined style={{ fontSize: 18 }} />} onClick={() => { setNotificationsVisible(true); loadNotifications(); loadMyInquiries(); }} />
          </Badge>

          <Button type="text" onClick={() => setProfileModalVisible(true)}>{summary?.realName || customerName}</Button>
          <Button type="text" icon={<LogoutOutlined />} onClick={() => navigate('/login')} />
        </Space>
      </Header>

      <Content style={{ padding: '32px 48px', maxWidth: 1600, margin: '0 auto', width: '100%' }}>
        <div
          className="animated-banner"
          style={{
            marginBottom: 40,
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            borderRadius: 24, padding: '40px 60px', color: 'white', position: 'relative', overflow: 'hidden'
          }}>
          <Row align="middle" gutter={48}>
            <Col flex="auto">
              <Title level={1} style={{ color: 'white', margin: '0 0 16px 0' }}>好书，好价，好时光。</Title>
              <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: 16 }}>
                尊敬的{summary ? (['一', '二', '三', '四', '五'][summary.creditLevelId - 1] || summary.creditLevelId) + '级会员' : '会员'}，我们为您精选了 {books.length} 本好书。当前折扣：{summary ? (summary.discountRate * 100).toFixed(0) : 100}%
              </Text>
            </Col>
            <Col>
              <div style={{ background: 'rgba(255,255,255,0.15)', backdropFilter: 'blur(10px)', borderRadius: 16, padding: '20px 30px' }}>
                <Statistic title={<span style={{ color: 'rgba(255,255,255,0.8)' }}>余额</span>} value={summary?.accountBalance} precision={2} prefix="¥" valueStyle={{ color: 'white', fontSize: 32 }} />
                <Button ghost size="small" style={{ marginTop: 8 }} onClick={handleRecharge}>充值</Button>
                <Button type="link" style={{ marginTop: 8, color: 'white' }} onClick={() => { setAddressModalVisible(true); loadAddresses(); }}>地址管理</Button>
              </div>
            </Col>
          </Row>
        </div>

        {/* 高级搜索结果返回按钮 */}
        {isAdvancedSearchResult && (
          <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
            <Button 
              icon={<HomeOutlined />} 
              onClick={resetToInitialView}
              type="default"
            >
              返回初始界面
            </Button>
          </div>
        )}

        <List
          grid={{ 
            gutter: [24, 24], 
            xs: 1, 
            sm: 2, 
            md: 3, 
            lg: 4, 
            xl: 5, 
            xxl: 6 
          }}
          dataSource={books}
          loading={loadingBooks}
          locale={{
            emptyText: (
              <Empty description="未找到相关书籍" image={Empty.PRESENTED_IMAGE_SIMPLE}>
                {keyword && (
                  <div style={{ marginTop: 16 }}>
                    <p style={{ marginBottom: 16 }}>找不到您要的书？您可以提交询价申请。</p>
                    <Button type="primary" onClick={() => {
                      setInquiryModalVisible(true);
                      inquiryForm.setFieldsValue({ bookTitle: keyword });
                    }}>
                      提交询价/报价申请
                    </Button>
                  </div>
                )}
              </Empty>
            )
          }}
          renderItem={book => (
            <List.Item style={{ height: '100%', display: 'flex' }}>
              <Card 
                hoverable 
                bordered={false} 
                style={{ 
                  borderRadius: 16, 
                  overflow: 'hidden', 
                  height: '100%',
                  width: '100%',
                  display: 'flex',
                  flexDirection: 'column'
                }}
                bodyStyle={{ 
                  flex: 1, 
                  display: 'flex', 
                  flexDirection: 'column',
                  padding: '20px'
                }}
                cover={
                  <div style={{ 
                    height: 280,
                    width: '100%',
                    background: '#f7f8fa', 
                    display: 'flex', 
                    alignItems: 'center', 
                    justifyContent: 'center', 
                    position: 'relative',
                    overflow: 'hidden',
                    flexShrink: 0
                  }}>
                    {book.coverImageUrl ? (
                      <img 
                        src={book.coverImageUrl} 
                        alt={book.title}
                        style={{ 
                          width: '100%', 
                          height: '100%', 
                          objectFit: 'cover',
                          display: 'block'
                        }} 
                      />
                    ) : (
                      <BookOutlined style={{ fontSize: 80, color: '#d1d5db' }} />
                    )}
                    {book.status !== 'AVAILABLE' && (
                      <div style={{ position: 'absolute', inset: 0, background: 'rgba(255,255,255,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Tag color="red">缺货</Tag>
                      </div>
                    )}
                    {/* 丛书标识 */}
                    {book.seriesFlag && (
                      <Tag color="purple" style={{ position: 'absolute', top: 8, left: 8, margin: 0 }}>📚 丛书</Tag>
                    )}
                    {book.parentBookId && (
                      <Tag color="blue" style={{ position: 'absolute', top: 8, left: 8, margin: 0 }}>子书</Tag>
                    )}
                  </div>
                }
                actions={[
                  <Button key="detail" type="text" style={{ fontSize: '14px' }} onClick={() => loadBookDetail(book.bookId)}>详情</Button>,
                  <Button key="cart" type="text" style={{ color: token.colorPrimary, fontSize: '14px' }} onClick={() => addToCart(book)}>加入购物车</Button>
                ]}
              >
                <Card.Meta
                  title={
                    <div style={{ 
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      display: '-webkit-box',
                      WebkitLineClamp: 2,
                      WebkitBoxOrient: 'vertical',
                      lineHeight: '1.5',
                      height: '3.5em',
                      marginBottom: '14px',
                      wordBreak: 'break-word',
                      fontSize: '15px',
                      fontWeight: 500
                    }}>
                      <Space wrap>
                        <span>{book.title}</span>
                        {book.seriesFlag && <Tag color="purple" style={{ fontSize: 11, margin: 0 }}>套装</Tag>}
                      </Space>
                    </div>
                  }
                  description={
                    <Space direction="vertical" size={6} style={{ width: '100%' }}>
                      <Text 
                        type="secondary" 
                        style={{ 
                          fontSize: 13,
                          display: 'block',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                          height: '20px',
                          lineHeight: '20px'
                        }}
                        title={book.publisher}
                      >
                        {book.publisher || '-'}
                      </Text>
                      <Text strong style={{ color: '#ff4d4f', fontSize: 18, display: 'block', height: '26px', lineHeight: '26px' }}>
                        ¥{book.price.toFixed(2)}
                      </Text>
                    </Space>
                  } 
                />
              </Card>
            </List.Item>
          )}
        />
      </Content>

      <Drawer title="购物车" open={cartVisible} onClose={() => setCartVisible(false)} width={400}
        footer={
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <Text type="secondary">总计: </Text>
              <span style={{ fontSize: 20, fontWeight: 'bold', color: token.colorPrimary }}>¥{totalAmount.toFixed(2)}</span>
            </div>
            <Button type="primary" onClick={submitOrder} disabled={!cart.length}>去结算</Button>
          </div>
        }
      >
        {/* 收货地址选择 */}
        <div style={{ marginBottom: 16, padding: 12, background: '#f9fafb', borderRadius: 8 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <Text strong>收货地址</Text>
            <Button type="link" size="small" onClick={() => setAddressModalVisible(true)}>管理地址</Button>
          </div>
          {addresses.length === 0 ? (
            <Text type="secondary">暂无地址，请先添加</Text>
          ) : (
            <Select
              style={{ width: '100%' }}
              placeholder="选择收货地址"
              value={selectedAddressId || (addresses.find(a => a.isDefault)?.addressId) || addresses[0]?.addressId}
              onChange={(val) => setSelectedAddressId(val)}
              options={addresses.map(a => ({
                value: a.addressId,
                label: `${a.receiver || ''} ${a.city || ''} ${a.detail}${a.isDefault ? ' (默认)' : ''}`
              }))}
            />
          )}
        </div>

        {cart.length === 0 ? <Empty description="空空如也" /> : (
          <List dataSource={cart} renderItem={item => (
            <List.Item actions={[<Button danger type="text" size="small" onClick={() => removeFromCart(item.bookId)}>移除</Button>]}>
              <List.Item.Meta
                title={item.title}
                description={
                  <Space>
                    <span>¥{item.unitPrice}</span>
                    <span>×</span>
                    <InputNumber
                      min={1}
                      max={99}
                      value={item.quantity}
                      size="small"
                      style={{ width: 60 }}
                      onChange={(v) => v && updateCartQuantityApi(item.bookId, v)}
                    />
                  </Space>
                }
              />
              <div style={{ fontWeight: 'bold' }}>¥{(item.unitPrice * item.quantity).toFixed(2)}</div>
            </List.Item>
          )} />
        )}
      </Drawer>

      <Modal title="我的订单" open={ordersVisible} onCancel={() => setOrdersVisible(false)} footer={null} width={900}>
        <Tabs defaultActiveKey="全部" onChange={loadOrders} items={[
          { label: '全部', key: '全部' },
          { label: '待付款', key: 'PENDING_PAYMENT' },
          { label: '待发货', key: 'PENDING_SHIPMENT' },
          { label: '配送中', key: 'DELIVERING' },
          { label: '已完成', key: 'COMPLETED' },
          { label: '已取消', key: 'CANCELLED' }
        ]} />
        <Table dataSource={orders} rowKey="orderId" size="small"
          columns={[
            { title: '订单号', dataIndex: 'orderId', render: v => <a onClick={() => openOrderDetail(v)}>{v}</a> },
            { title: '时间', dataIndex: 'orderTime', width: 170 },
            {
              title: '状态', dataIndex: 'orderStatus',
              render: v => {
                const statusMap: Record<string, { label: string; color: string }> = {
                  'PENDING_PAYMENT': { label: '待付款', color: 'warning' },
                  'OUT_OF_STOCK_PENDING': { label: '缺货待处理', color: 'orange' },
                  'PENDING_SHIPMENT': { label: '待发货', color: 'processing' },
                  'SHIPPED': { label: '已发货', color: 'blue' },
                  'DELIVERING': { label: '配送中', color: 'processing' },
                  'COMPLETED': { label: '已完成', color: 'success' },
                  'CANCELLED': { label: '已取消', color: 'error' }
                };
                const status = statusMap[v] || { label: v, color: 'default' };
                return <Tag color={status.color}>{status.label}</Tag>;
              }
            },
            { title: '金额', dataIndex: 'payableAmount', render: v => `¥${v.toFixed(2)}` },
            {
              title: '操作', render: (_, r) => {
                const isUnpaid = !r.paymentTime;
                const isDeliveringUnpaid = r.orderStatus === 'DELIVERING' && isUnpaid;
                // 待付款状态且已发货（先发货后付款的情况）
                const isPendingPaymentWithShipment = r.orderStatus === 'PENDING_PAYMENT' && isUnpaid && r.hasShipments;
                // 待付款状态且未发货（正常待付款）
                const isPendingPaymentWithoutShipment = r.orderStatus === 'PENDING_PAYMENT' && isUnpaid && !r.hasShipments;
                
                return (
                <Space>
                    {/* 正常待付款：显示普通付款按钮和取消按钮 */}
                    {isPendingPaymentWithoutShipment && (
                      <>
                        <Button type="primary" size="small" onClick={() => payOrder(r.orderId)}>付款</Button>
                    <Popconfirm title="确定取消此订单？" onConfirm={() => cancelOrder(r.orderId)}>
                      <Button danger size="small">取消</Button>
                        </Popconfirm>
                      </>
                    )}
                    {/* 缺货待处理：显示付款按钮和取消按钮 */}
                    {r.orderStatus === 'OUT_OF_STOCK_PENDING' && (
                      <>
                        <Button type="primary" size="small" onClick={() => payOrder(r.orderId)}>付款</Button>
                        <Popconfirm title="确定取消此订单？" onConfirm={() => cancelOrder(r.orderId)}>
                          <Button danger size="small">取消</Button>
                        </Popconfirm>
                      </>
                    )}
                    {/* 配送中且未付款时，显示特殊标注的付款按钮和收货按钮 */}
                    {isDeliveringUnpaid && (
                      <>
                        <Button 
                          type="primary" 
                          size="small" 
                          onClick={() => payOrder(r.orderId)}
                          style={{ borderColor: '#ff9800', background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)' }}
                        >
                          💳 付款（信用特权）
                        </Button>
                        <Button size="small" onClick={() => openReceiveModal(r.orderId)}>收货</Button>
                      </>
                    )}
                    {/* 待付款但已发货（收货完成后未付款）：显示特殊付款按钮，不显示取消和收货按钮 */}
                    {isPendingPaymentWithShipment && (
                      <Button 
                        type="primary" 
                        size="small" 
                        onClick={() => payOrder(r.orderId)}
                        style={{ borderColor: '#ff9800', background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)' }}
                      >
                        💳 付款（信用特权）
                      </Button>
                    )}
                    {/* 已发货或配送中且已付款：只显示收货按钮 */}
                    {((r.orderStatus === 'SHIPPED' || r.orderStatus === 'DELIVERING') && !isDeliveringUnpaid) && (
                      <Button size="small" onClick={() => openReceiveModal(r.orderId)}>收货</Button>
                    )}
                </Space>
                );
              }
            }
          ]}
        />
      </Modal>

      <Modal title="订单详情" open={orderDetailVisible} onCancel={() => setOrderDetailVisible(false)} footer={null} width={800}>
        {activeOrderDetail && (
          <Space direction="vertical" style={{ width: '100%' }} size="large">
            <Descriptions size="small" bordered column={2}>
              <Descriptions.Item label="订单号">{activeOrderDetail.order.orderId}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag>{
                ({ 'PENDING_PAYMENT': '待付款', 'OUT_OF_STOCK_PENDING': '缺货待处理', 'PENDING_SHIPMENT': '待发货', 'SHIPPED': '已发货', 'DELIVERING': '配送中', 'COMPLETED': '已完成', 'CANCELLED': '已取消' } as Record<string, string>)[activeOrderDetail.order.orderStatus] || activeOrderDetail.order.orderStatus
              }</Tag></Descriptions.Item>
              <Descriptions.Item label="下单时间">{activeOrderDetail.order.orderTime}</Descriptions.Item>
              <Descriptions.Item label="金额">¥{activeOrderDetail.order.payableAmount.toFixed(2)}</Descriptions.Item>
              <Descriptions.Item label="收货地址" span={2}>{activeOrderDetail.order.shippingAddressSnapshot}</Descriptions.Item>
            </Descriptions>

            {activeOrderDetail.shipments && activeOrderDetail.shipments.length > 0 && (
              <div style={{ background: '#f9f9f9', padding: 12, borderRadius: 8 }}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>物流信息</div>
                {activeOrderDetail.shipments.map(s => (
                  <div key={s.shipmentId} style={{ fontSize: 13, color: '#555' }}>
                    <Tag color="blue">{s.carrier}</Tag> 单号: {s.trackingNumber} <span style={{ marginLeft: 8, color: '#999' }}>{s.shipTime}</span>
                  </div>
                ))}
              </div>
            )}

            <Table dataSource={activeOrderDetail.items} size="small" pagination={false}
              columns={[
                { title: '书名', dataIndex: 'bookId', render: id => books.find(b => b.bookId === id)?.title || id },
                { title: '单价', dataIndex: 'unitPrice', render: v => `¥${v}` },
                { title: '数量', dataIndex: 'quantity' },
                { title: '小计', render: (_, r) => `¥${(r.quantity * r.unitPrice).toFixed(2)}` },
                { title: '发货/收货', render: (_, r) => <Tag>{`${r.shippedQuantity || 0} / ${r.receivedQuantity || 0}`}</Tag> },
              ]}
            />
            
            {/* 操作按钮 */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12, marginTop: 16 }}>
              {(() => {
                const order = activeOrderDetail.order;
                const isUnpaid = !order.paymentTime;
                const isDeliveringUnpaid = order.orderStatus === 'DELIVERING' && isUnpaid;
                // 待付款状态且已发货（先发货后付款的情况）
                const hasShipments = activeOrderDetail.shipments && activeOrderDetail.shipments.length > 0;
                const isPendingPaymentWithShipment = order.orderStatus === 'PENDING_PAYMENT' && isUnpaid && hasShipments;
                // 待付款状态且未发货（正常待付款）
                const isPendingPaymentWithoutShipment = order.orderStatus === 'PENDING_PAYMENT' && isUnpaid && !hasShipments;
                
                return (
                  <Space>
                    {/* 正常待付款：显示普通付款按钮和取消按钮 */}
                    {isPendingPaymentWithoutShipment && (
                      <>
                        <Button type="primary" onClick={() => payOrder(order.orderId)}>付款</Button>
                        <Popconfirm title="确定取消此订单？" onConfirm={() => cancelOrder(order.orderId)}>
                          <Button danger>取消订单</Button>
                        </Popconfirm>
                      </>
                    )}
                    {/* 缺货待处理：显示付款按钮和取消按钮 */}
                    {order.orderStatus === 'OUT_OF_STOCK_PENDING' && (
                      <>
                        <Button type="primary" onClick={() => payOrder(order.orderId)}>付款</Button>
                        <Popconfirm title="确定取消此订单？" onConfirm={() => cancelOrder(order.orderId)}>
                          <Button danger>取消订单</Button>
                        </Popconfirm>
                      </>
                    )}
                    {/* 配送中且未付款时，显示特殊标注的付款按钮和收货按钮 */}
                    {isDeliveringUnpaid && (
                      <>
                        <Button 
                          type="primary" 
                          onClick={() => payOrder(order.orderId)}
                          style={{ borderColor: '#ff9800', background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)' }}
                        >
                          💳 付款（信用特权）
                        </Button>
                        <Button onClick={() => openReceiveModal(order.orderId)}>收货</Button>
                      </>
                    )}
                    {/* 待付款但已发货（收货完成后未付款）：显示特殊付款按钮，不显示取消和收货按钮 */}
                    {isPendingPaymentWithShipment && (
                      <Button 
                        type="primary" 
                        onClick={() => payOrder(order.orderId)}
                        style={{ borderColor: '#ff9800', background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)' }}
                      >
                        💳 付款（信用特权）
                      </Button>
                    )}
                    {/* 已发货或配送中且已付款：只显示收货按钮 */}
                    {((order.orderStatus === 'SHIPPED' || order.orderStatus === 'DELIVERING') && !isDeliveringUnpaid) && (
                      <Button onClick={() => openReceiveModal(order.orderId)}>收货</Button>
                    )}
                  </Space>
                );
              })()}
            </div>
          </Space>
        )}
      </Modal>

      <Modal 
        title="确认收货" 
        open={receiveModalVisible} 
        onCancel={() => {
          setReceiveModalVisible(false);
          setSelectedShipmentId(null);
        }} 
        onOk={handleConfirmReceive}
      >
        {activeOrderDetail && (() => {
          // 优先使用shipmentsWithItems，如果没有则使用shipments（向后兼容）
          let shipmentsWithItems = activeOrderDetail.shipmentsWithItems;
          if (!shipmentsWithItems || shipmentsWithItems.length === 0) {
            // 如果shipmentsWithItems不存在，从shipments创建，但items需要从后端获取
            // 这里暂时设为空数组，但实际上应该已经有shipmentsWithItems了
            shipmentsWithItems = (activeOrderDetail.shipments || []).map(s => ({ shipment: s, items: [] }));
          }
          // 按shipment_id排序，确保编号计算基于稳定的顺序
          shipmentsWithItems = [...shipmentsWithItems].sort((a, b) => 
            (a.shipment.shipmentId || 0) - (b.shipment.shipmentId || 0)
          );
          const allShipments = shipmentsWithItems.map(swi => swi.shipment);
          const isPartialShipment = allShipments.length > 1;
          
          if (isPartialShipment) {
            // 分次发货：显示shipment列表供选择
            const shippableShipments = shipmentsWithItems.filter(swi => swi.shipment.shipmentStatus === 'SHIPPED');
            return (
              <div>
                <Text type="secondary" style={{ marginBottom: 16, display: 'block' }}>
                  请选择要确认收货的子发货单（只能选择状态为"运送中"的发货单）
                </Text>
                <Radio.Group 
                  value={selectedShipmentId} 
                  onChange={(e) => setSelectedShipmentId(e.target.value)}
                  style={{ width: '100%' }}
                >
                  <Space direction="vertical" style={{ width: '100%' }}>
                    {shippableShipments.map((swi) => {
                      // 在所有shipments中找到当前shipment的索引，用于显示编号（基于未过滤的allShipments）
                      const shipmentIndex = allShipments.findIndex(s => s.shipmentId === swi.shipment.shipmentId);
                      const shipmentNumber = shipmentIndex + 1;
                      return (
                        <Radio key={swi.shipment.shipmentId} value={swi.shipment.shipmentId}>
                          <div style={{ width: '100%' }}>
                            <div><strong>子发货 {shipmentNumber}</strong></div>
                            <div style={{ fontSize: 12, color: '#888', marginTop: 4 }}>
                              {swi.shipment.carrier} - {swi.shipment.trackingNumber} {swi.shipment.shipTime && `(${swi.shipment.shipTime})`}
                            </div>
                            <div style={{ marginTop: 8, padding: 8, background: '#f5f5f5', borderRadius: 4 }}>
                              <Text type="secondary" style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>包含书籍：</Text>
                              {(() => {
                                // 调试日志
                                if (!swi.items) {
                                  console.log('Shipment', swi.shipment.shipmentId, 'has no items property');
                                } else if (!Array.isArray(swi.items)) {
                                  console.log('Shipment', swi.shipment.shipmentId, 'items is not an array:', swi.items);
                                } else if (swi.items.length === 0) {
                                  console.log('Shipment', swi.shipment.shipmentId, 'items array is empty');
                                } else {
                                  console.log('Shipment', swi.shipment.shipmentId, 'items:', swi.items);
                                }
                                return swi.items && Array.isArray(swi.items) && swi.items.length > 0 ? (
                                  swi.items.map((item, idx) => {
                                    const book = books.find(b => b.bookId === item.bookId);
                                    if (!book && item.bookId) {
                                      console.log('Book not found in books array for bookId:', item.bookId);
                                    }
                                    return (
                                      <div key={idx} style={{ fontSize: 12, color: '#666', marginLeft: 8 }}>
                                        {book?.title || item.bookId || '未知书籍'} × {item.shipQuantity || 0}
                                      </div>
                                    );
                                  })
                                ) : (
                                  <Text type="secondary" style={{ fontSize: 12, marginLeft: 8 }}>暂无书籍信息</Text>
                                );
                              })()}
                            </div>
                          </div>
                        </Radio>
                      );
                    })}
                  </Space>
                </Radio.Group>
              </div>
            );
          } else {
            // 整体发货：显示书籍清单，直接确认收货全部
            if (allShipments.length === 0) {
              return <Text>暂无发货信息</Text>;
            }
            const shipment = allShipments[0];
            if (!shipment) {
              return <Text>暂无发货信息</Text>;
            }
            // 优先使用shipmentsWithItems中的items，如果没有则使用orderDetail中的items
            const shipmentWithItems = shipmentsWithItems.find(swi => swi.shipment.shipmentId === shipment.shipmentId);
            let receivableItems: Array<{ orderItemId: number; bookId: string; shippedQuantity: number; receivedQuantity: number }>;
            if (shipmentWithItems && shipmentWithItems.items && shipmentWithItems.items.length > 0) {
              // 使用shipment中的items
              receivableItems = shipmentWithItems.items.map(item => {
                const orderItem = activeOrderDetail.items.find(oi => oi.orderItemId === item.orderItemId);
                return {
                  orderItemId: item.orderItemId,
                  bookId: item.bookId,
                  shippedQuantity: item.shipQuantity || 0,
                  receivedQuantity: orderItem?.receivedQuantity || 0
                };
              }).filter(i => i.shippedQuantity > i.receivedQuantity);
            } else {
              // 回退到使用orderDetail中的items
              receivableItems = (activeOrderDetail.items || []).map(i => ({
                orderItemId: i.orderItemId,
                bookId: i.bookId,
                shippedQuantity: i.shippedQuantity || 0,
                receivedQuantity: i.receivedQuantity || 0
              })).filter(i => i.shippedQuantity > i.receivedQuantity);
            }
            
            if (!receivableItems || receivableItems.length === 0) {
              return <Text>暂无待收货的书籍</Text>;
            }
            return (
              <div>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>物流信息：</Text>
                  <div style={{ marginTop: 8, padding: 8, background: '#f9f9f9', borderRadius: 4 }}>
                    <Tag color="blue">{shipment.carrier}</Tag> {shipment.trackingNumber}
                    {shipment.shipTime && <span style={{ marginLeft: 8, color: '#888' }}>{shipment.shipTime}</span>}
                  </div>
                </div>
                <div style={{ marginBottom: 16 }}>
                  <Text strong>本次将确认收货以下书籍：</Text>
                </div>
                <Table 
                  dataSource={receivableItems} 
                  size="small" 
                  pagination={false}
                  columns={[
                    { 
                      title: '书名', 
                      dataIndex: 'bookId', 
                      render: id => books.find(b => b.bookId === id)?.title || id 
                    },
                    { 
                      title: '数量', 
                      render: (_, r) => (r.shippedQuantity || 0) - (r.receivedQuantity || 0)
                    }
                  ]}
                />
                <div style={{ marginTop: 16, padding: 12, background: '#e6f7ff', borderRadius: 4 }}>
                  <Text type="secondary">点击确认后将收货以上所有书籍</Text>
                </div>
              </div>
            );
          }
        })()}
      </Modal>

      {/* Shortage Registration Modal */}
      <Modal
        title="缺书登记"
        open={shortageModalVisible}
        footer={null}
        onCancel={() => {
          // 用户点X或取消：不创建订单，购物车商品保留
          setShortageModalVisible(false);
          setShortageNote('');
          delete (window as any).__pendingOrderPayload;
        }}
      >
        <div style={{ marginBottom: 16 }}>
          <Text>以下图书当前库存不足，请选择处理方式：</Text>
        </div>
        <Table dataSource={shortages} size="small" pagination={false} rowKey="bookId"
          columns={[
            { title: '书号', dataIndex: 'bookId' },
            { title: '订购数量', dataIndex: 'quantity' },
            { title: '当前库存', dataIndex: 'currentStock' },
          ]}
        />
        <div style={{ marginTop: 16 }}>
          <Text type="secondary">额外请求备注（可选）：</Text>
          <Input.TextArea
            placeholder="例如：希望本书到货后第一时间通知我..."
            rows={2}
            value={shortageNote}
            onChange={e => setShortageNote(e.target.value)}
            style={{ marginTop: 8 }}
          />
        </div>
        <div style={{ marginTop: 24, display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
          <Button onClick={() => {
            // 取消：不创建订单，购物车保留
            setShortageModalVisible(false);
            setShortageNote('');
            delete (window as any).__pendingOrderPayload;
          }}>取 消</Button>
          <Button onClick={async () => {
            // 仅提交缺书登记(暂不付款)：先创建订单，再提交缺书登记
            const payload = (window as any).__pendingOrderPayload;
            if (!payload) { message.error('订单数据丢失'); return; }
            try {
              // 创建订单
              const resp = await http.post<SalesOrderDto>(`/customer/${customerId}/orders`, payload);
              const order = resp.data;
              // 提交缺书登记
              await http.post(`/customer/orders/${order.orderId}/shortages/decision`, {
                decision: 'REQUEST_ONLY',
                customerNote: shortageNote
              });
              message.success('缺书登记已提交（暂未付款），等待管理员审核');
              await clearCartApi();
              setShortageModalVisible(false);
              setShortageNote('');
              setCartVisible(false);
              delete (window as any).__pendingOrderPayload;
              await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
            } catch (e: any) {
              message.error(e?.response?.data?.message || '处理失败');
            }
          }}>仅提交缺书登记(暂不付款)</Button>
          <Button type="primary" onClick={async () => {
            // 付款并生成缺书记录：先创建订单，再提交并付款
            const payload = (window as any).__pendingOrderPayload;
            if (!payload) { message.error('订单数据丢失'); return; }
            try {
              // 创建订单
              const resp = await http.post<SalesOrderDto>(`/customer/${customerId}/orders`, payload);
              const order = resp.data;
              // 提交缺书登记并付款
              await http.post(`/customer/orders/${order.orderId}/shortages/decision`, {
                decision: 'PAY_AND_CREATE',
                customerNote: shortageNote
              });
              message.success('缺书登记已提交并已付款，等待到货后发货');
              await clearCartApi();
              setShortageModalVisible(false);
              setShortageNote('');
              setCartVisible(false);
              delete (window as any).__pendingOrderPayload;
              await Promise.all([loadSummary(), loadOrders(orderStatusFilter)]);
            } catch (e: any) {
              message.error(e?.response?.data?.message || '处理失败');
            }
          }}>付款并生成缺书记录</Button>
        </div>
      </Modal>

      <Modal title="地址管理" open={addressModalVisible} onCancel={() => setAddressModalVisible(false)} footer={null}>
        <Button
          block
          type={addressFormVisible ? "default" : "dashed"}
          onClick={() => setAddressFormVisible(!addressFormVisible)}
          style={{ marginBottom: 16 }}
          icon={addressFormVisible ? <CloseOutlined /> : undefined}
        >
          {addressFormVisible ? '收起' : '新增地址'}
        </Button>
        {addressFormVisible && (
          <Card size="small" style={{ marginBottom: 16 }}>
            <Form onFinish={handleAddAddress} layout="vertical">
              <Form.Item name="receiver" label="收件人" rules={[{ required: true, message: '请输入收件人' }]}><Input /></Form.Item>
              <Form.Item name="phone" label="电话" rules={[{ required: true, message: '请输入电话' }]}><Input /></Form.Item>
              <Form.Item name="city" label="城市"><Input /></Form.Item>
              <Form.Item name="detail" label="详细地址"><Input /></Form.Item>
              <Form.Item name="isDefault" valuePropName="checked"><Checkbox>设为默认</Checkbox></Form.Item>
              <Space>
                <Button type="primary" htmlType="submit">保存</Button>
                <Button onClick={() => setAddressFormVisible(false)}>取消</Button>
              </Space>
            </Form>
          </Card>
        )}
        <List dataSource={addresses} renderItem={item => (
          <List.Item actions={[
            !item.isDefault && <a onClick={() => setDefaultAddress(item.addressId)}>设默认</a>,
            <Popconfirm title="确定删除？" onConfirm={() => deleteAddress(item.addressId)}>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
          ]}>
            <List.Item.Meta
              title={
                <Space>
                  <span>{item.receiver} {item.phone}</span>
                  {item.isDefault && <Tag color="blue">默认</Tag>}
                </Space>
              }
              description={`${item.province || ''}${item.city || ''}${item.district || ''} ${item.detail}`}
            />
          </List.Item>
        )} />
      </Modal>

      <Modal title="个人信息" open={profileModalVisible} onCancel={() => setProfileModalVisible(false)} footer={null}>
        {summary && (
          <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
            <Descriptions.Item label="账号">{summary.username}</Descriptions.Item>
            <Descriptions.Item label="会员等级">{summary.creditLevelName} (Lv.{summary.creditLevelId})</Descriptions.Item>
            <Descriptions.Item label="当前折扣">{(summary.discountRate * 100).toFixed(0)}%</Descriptions.Item>
            <Descriptions.Item label="账户余额">¥{summary.accountBalance.toFixed(2)}</Descriptions.Item>
          </Descriptions>
        )}
        <div style={{ fontWeight: 'bold', marginBottom: 12 }}>修改资料</div>
        <Form initialValues={summary as any} onFinish={handleUpdateProfile} layout="vertical">
          <Form.Item name="realName" label="真实姓名"><Input /></Form.Item>
          <Form.Item name="email" label="邮箱"><Input /></Form.Item>
          <Form.Item name="mobilePhone" label="手机"><Input /></Form.Item>
          <Button type="primary" htmlType="submit" block>保存修改</Button>
        </Form>
      </Modal>

      <Modal title="消息中心" open={notificationsVisible} onCancel={() => setNotificationsVisible(false)} footer={null} width={600}>
        <Segmented
          options={[
            { label: `消息通知 (${notifications.filter(n => !n.readFlag).length})`, value: 'messages' },
            { label: `我的询价 (${myInquiries.length})`, value: 'inquiries' }
          ]}
          value={notificationTab}
          onChange={(v) => setNotificationTab(v as 'messages' | 'inquiries')}
          block
          style={{ marginBottom: 16 }}
        />
        {notificationTab === 'messages' ? (
          <>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>共 {notifications.length} 条消息</span>
              <Space>
                {notifications.some(n => !n.readFlag) && <Button size="small" onClick={markAllRead}>全部标为已读</Button>}
                {notifications.length > 0 && (
                  <Popconfirm title="确定清除所有通知？" onConfirm={clearAllNotifications}>
                    <Button size="small" danger>清除全部</Button>
                  </Popconfirm>
                )}
              </Space>
            </div>
            <List dataSource={notifications} renderItem={item => (
              <List.Item
                key={item.notificationId}
                style={{ opacity: item.readFlag ? 0.6 : 1, transition: 'opacity 0.3s' }}
                actions={[
                  !item.readFlag && <Button type="link" size="small" onClick={() => markRead(item.notificationId)}>已读</Button>,
                  <Popconfirm title="确定删除？" onConfirm={() => deleteNotification(item.notificationId)}>
                    <Button type="text" size="small" danger icon={<DeleteOutlined />} />
                  </Popconfirm>
                ]}
              >
                <List.Item.Meta
                  avatar={<Badge status={item.readFlag ? 'default' : 'processing'} />}
                  title={item.title}
                  description={<div>{item.content} <span style={{ fontSize: 12, color: '#aaa', marginLeft: 8 }}>{item.createdTime}</span></div>}
                />
              </List.Item>
            )} />
            {notifications.length === 0 && <Empty description="暂无通知" />}
          </>
        ) : (
          <>
            <List
              dataSource={myInquiries}
              loading={loadingMyInquiries}
              renderItem={renderMyInquiryItem}
              locale={{ emptyText: <Empty description="暂无询价记录" /> }}
            />
          </>
        )}
      </Modal>

      <Modal
        title="书籍询价/报价申请"
        open={inquiryModalVisible}
        onCancel={() => setInquiryModalVisible(false)}
        onOk={() => inquiryForm.submit()}
        confirmLoading={false}
      >
        <Form form={inquiryForm} layout="vertical" onFinish={submitInquiry}>
          <Form.Item name="bookTitle" label="书名" rules={[{ required: true, message: '请输入书名' }]}>
            <Input placeholder="请输入书名" />
          </Form.Item>
          <Form.Item name="bookAuthor" label="作者">
            <Input placeholder="请输入作者" />
          </Form.Item>
          <Form.Item name="publisher" label="出版社">
            <Input placeholder="请输入出版社" />
          </Form.Item>
          <Form.Item name="isbn" label="ISBN">
            <Input placeholder="选填，若知道ISBN更佳" />
          </Form.Item>
          <Form.Item name="quantity" label="需求数量" initialValue={1} rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="customerNote" label="备注">
            <Input.TextArea placeholder="其他需求说明" rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 高级搜索 Modal */}
      <Modal
        title="高级搜索"
        open={advSearchVisible}
        onCancel={() => setAdvSearchVisible(false)}
        onOk={advancedSearch}
        confirmLoading={advSearchLoading}
        okText="搜索"
        width={500}
      >
        <div style={{ marginBottom: 16 }}>
          <Segmented
            options={[
              { label: '按作者', value: 'author' },
              { label: '按关键字', value: 'keywords' }
            ]}
            value={advSearchType}
            onChange={(v) => setAdvSearchType(v as 'author' | 'keywords')}
            block
          />
        </div>

        {advSearchType === 'author' ? (
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Typography.Text strong>作者名：</Typography.Text>
              <Input
                placeholder="输入作者名（模糊匹配）"
                value={advAuthorName}
                onChange={e => setAdvAuthorName(e.target.value)}
                style={{ marginTop: 4 }}
              />
            </div>
            <div>
              <Typography.Text strong>作者顺序：</Typography.Text>
              <Select
                value={advAuthorOrder}
                onChange={setAdvAuthorOrder}
                style={{ width: '100%', marginTop: 4 }}
                options={[
                  { label: '不限', value: 0 },
                  { label: '第一作者', value: 1 },
                  { label: '第二作者', value: 2 },
                  { label: '第三作者', value: 3 },
                  { label: '第四作者', value: 4 }
                ]}
              />
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                可选择仅搜索第一作者、第二作者等
              </Typography.Text>
            </div>
          </Space>
        ) : (
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Typography.Text strong>关键字列表：</Typography.Text>
              <Input
                placeholder="输入多个关键字，用逗号分隔，如：数据库,SQL,编程"
                value={advKeywords}
                onChange={e => setAdvKeywords(e.target.value)}
                style={{ marginTop: 4 }}
              />
            </div>
            <div>
              <Typography.Text strong>最低匹配数：</Typography.Text>
              <InputNumber
                min={1}
                max={10}
                value={advMinMatch}
                onChange={v => setAdvMinMatch(v || 1)}
                style={{ width: '100%', marginTop: 4 }}
              />
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                需要匹配几个关键字才显示结果（匹配程度）
              </Typography.Text>
            </div>
          </Space>
        )}
      </Modal>

      <Modal
        open={!!detailBook}
        title={detailBook?.book.title}
        onCancel={() => setDetailBook(null)}
        footer={null}
        width={600}
      >
        {loadingDetail ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : detailBook && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="ISBN">{detailBook.book.isbn || '-'}</Descriptions.Item>
              <Descriptions.Item label="版次">{detailBook.book.edition || '-'}</Descriptions.Item>
              <Descriptions.Item label="出版社" span={2}>{detailBook.book.publisher || '-'}</Descriptions.Item>
              <Descriptions.Item label="出版日期">{detailBook.book.publishDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="价格">
                <span style={{ color: '#ff4d4f', fontSize: 18, fontWeight: 'bold' }}>
                  ¥{detailBook.book.price?.toFixed(2)}
                </span>
              </Descriptions.Item>
            </Descriptions>

            {detailBook.authors.length > 0 && (
              <div>
                <Text strong style={{ display: 'block', marginBottom: 8 }}>作者</Text>
                <Space wrap>
                  {detailBook.authors.map(author => (
                    <Tag key={author.authorId} color="blue">
                      {author.authorName}{author.nationality && ` (${author.nationality})`}
                    </Tag>
                  ))}
                </Space>
              </div>
            )}

            {detailBook.keywords.length > 0 && (
              <div>
                <Text strong style={{ display: 'block', marginBottom: 8 }}>关键词</Text>
                <Space wrap>
                  {detailBook.keywords.map(kw => (
                    <Tag key={kw.keywordId} color="green">{kw.keywordText}</Tag>
                  ))}
                </Space>
              </div>
            )}

            {detailBook.book.catalog && (
              <div>
                <Text strong style={{ display: 'block', marginBottom: 8 }}>目录</Text>
                <div style={{
                  background: '#f5f5f5',
                  padding: 12,
                  borderRadius: 8,
                  maxHeight: 200,
                  overflow: 'auto',
                  whiteSpace: 'pre-wrap',
                  fontSize: 13
                }}>
                  {detailBook.book.catalog}
                </div>
              </div>
            )}

            {/* 丛书包含的子书目 */}
            {detailBook.childBooks && detailBook.childBooks.length > 0 && (
              <div>
                <Text strong style={{ display: 'block', marginBottom: 8 }}>
                  📚 本丛书包含 {detailBook.childBooks.length} 本书
                </Text>
                <List
                  dataSource={detailBook.childBooks}
                  renderItem={(child) => (
                    <List.Item
                      actions={[
                        <Button size="small" type="link" onClick={() => loadBookDetail(child.bookId)}>查看</Button>,
                        <Button size="small" type="link" onClick={() => addToCart(child)}>加购</Button>
                      ]}
                    >
                      <List.Item.Meta
                        title={<span>{child.title}</span>}
                        description={<Text type="secondary">¥{child.price?.toFixed(2)}</Text>}
                      />
                    </List.Item>
                  )}
                />
              </div>
            )}
          </Space>
        )}
      </Modal>

    </Layout >
  );
};

export default CustomerLayout;
