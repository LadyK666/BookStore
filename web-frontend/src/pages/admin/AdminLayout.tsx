import React, { useState, useEffect } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer
} from 'recharts';
import {
  Layout,
  Menu,
  Table,
  Tag,
  Typography,
  Space,
  Modal,
  Descriptions,
  Select,
  message,
  InputNumber,
  Button,
  Popconfirm,
  Checkbox,
  ConfigProvider,
  Card,
  Row,
  Col,
  Statistic,
  Input,
  Form
} from 'antd';
import {
  DashboardOutlined,
  ShoppingOutlined,
  CarOutlined,
  AppstoreOutlined,
  ShopOutlined,
  BookOutlined,
  TeamOutlined,
  UsergroupAddOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useLocation, useNavigate } from 'react-router-dom';
import { http } from '../../api/http';

const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;




interface LocationState {
  adminName: string;
}

interface SalesOrderDto {
  orderId: number;
  customerId: number;
  orderTime: string;
  orderStatus: string;
  payableAmount: number;
  shippingAddressSnapshot?: string;
}

interface SalesOrderItemDto {
  orderItemId: number;
  bookId: string;
  quantity: number;
  unitPrice: number;
  subAmount: number;
  itemStatus: string;
  shippedQuantity?: number | null;
  receivedQuantity?: number | null;
}

interface ShipmentDto {
  shipmentId: number;
  carrier: string;
  trackingNumber: string;
  shipTime?: string | null;
  shipmentStatus: string;
  operator?: string | null;
}

interface OrderDetailResp {
  order: SalesOrderDto;
  items: SalesOrderItemDto[];
  shipments: ShipmentDto[];
}

interface InventoryDto {
  bookId: string;
  quantity: number;
  safetyStock: number;
  locationCode?: string;
}

interface OutOfStockRecordDto {
  recordId: number;
  bookId: string;
  requiredQuantity: number;
  status: string;
}

interface CustomerOosRequestDto {
  requestId: number;
  orderId: number;
  customerId: number;
  bookId: string;
  requestedQty: number;
  customerNote?: string;
}

interface PurchaseOrderDto {
  purchaseOrderId: number;
  supplierId: number;
  createDate?: string | null;
  expectedDate?: string | null;
  buyer: string;
  estimatedAmount: number;
  status: string;
}

interface CustomerDto {
  customerId: number;
  username: string;
  realName: string;
  accountBalance: number;
  totalConsumption: number;
  creditLevelId: number;
}

interface BookDto {
  bookId: string;
  isbn?: string;
  title: string;
  publisher?: string;
  price: number;
  status: string;
  // 丛书字段
  seriesFlag?: boolean;
  parentBookId?: string | null;
}

interface AuthorDto {
  authorId: number;
  authorName: string;
  nationality?: string;
  biography?: string;
  authorOrder?: number;
}

interface KeywordDto {
  keywordId: number;
  keywordText: string;
}

interface SupplyDto {
  supplierId: number;
  bookId: string;
  supplyPrice?: number;
  leadTimeDays?: number | null;
  primary: boolean;
}

interface BookInquiryRequestDto {
  inquiryId: number;
  customerId: number;
  bookTitle: string;
  bookAuthor?: string;
  publisher?: string;
  isbn?: string;
  quantity: number;
  customerNote?: string;
  inquiryTime: string;
  status: string;
  adminReply?: string;
  quotedPrice?: number;
  replyTime?: string;
}

type MenuKey = 'dashboard' | 'orders' | 'shipments' | 'inventory' | 'purchase' | 'customer' | 'supplier' | 'book' | 'inquiry';

const AdminLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state || {}) as Partial<LocationState>;

  // 从 localStorage 读取或从 location.state 获取管理员名称
  const getAdminName = () => {
    if (state.adminName) {
      localStorage.setItem('adminName', state.adminName);
      return state.adminName;
    }
    return localStorage.getItem('adminName') || 'admin';
  };
  const adminName = getAdminName();

  // Initialize selectedKey from URL hash for persistence across refresh
  const getInitialMenuKey = (): MenuKey => {
    const hash = window.location.hash.replace('#', '');
    const validKeys: MenuKey[] = ['dashboard', 'orders', 'shipments', 'inventory', 'purchase', 'customer', 'supplier', 'book'];
    if (validKeys.includes(hash as MenuKey)) {
      return hash as MenuKey;
    }
    return 'dashboard';
  };
  const [selectedKey, setSelectedKey] = useState<MenuKey>(getInitialMenuKey);
  const [orders, setOrders] = useState<SalesOrderDto[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [orderStatusFilter, setOrderStatusFilter] = useState<string>('全部');
  const [activeOrderDetail, setActiveOrderDetail] = useState<OrderDetailResp | null>(null);
  const [orderDetailVisible, setOrderDetailVisible] = useState(false);

  // Inquiry State
  const [inquiries, setInquiries] = useState<BookInquiryRequestDto[]>([]);
  const [loadingInquiries, setLoadingInquiries] = useState(false);
  const [inquiryReplyModalVisible, setInquiryReplyModalVisible] = useState(false);
  const [currentInquiry, setCurrentInquiry] = useState<BookInquiryRequestDto | null>(null);
  const [inquiryForm] = Form.useForm();
  const [inquiryActionType, setInquiryActionType] = useState<'quote' | 'reject'>('quote');

  const loadInquiries = async () => {
    setLoadingInquiries(true);
    try {
      const resp = await http.get<BookInquiryRequestDto[]>('/admin/inquiries');
      setInquiries(resp.data);
    } catch (e) {
      message.error('加载询价失败');
    } finally {
      setLoadingInquiries(false);
    }
  };

  const handleInquiryAction = (record: BookInquiryRequestDto, type: 'quote' | 'reject') => {
    setCurrentInquiry(record);
    setInquiryActionType(type);
    inquiryForm.resetFields();
    setInquiryReplyModalVisible(true);
  };

  const submitInquiryReply = async () => {
    if (!currentInquiry) return;
    try {
      const values = await inquiryForm.validateFields();
      if (inquiryActionType === 'quote') {
        await http.put(`/admin/inquiries/${currentInquiry.inquiryId}/quote`, values);
      } else {
        await http.put(`/admin/inquiries/${currentInquiry.inquiryId}/reject`, values);
      }
      message.success('操作成功');
      setInquiryReplyModalVisible(false);
      loadInquiries();
    } catch (e) {
      message.error('操作失败');
    }
  };
  const [shipmentOrders, setShipmentOrders] = useState<SalesOrderDto[]>([]);
  const [loadingShipOrders, setLoadingShipOrders] = useState(false);
  const [shipmentStatusFilter, setShipmentStatusFilter] = useState<string>('全部');
  const [shipModalVisible, setShipModalVisible] = useState(false);
  const [partialModalVisible, setPartialModalVisible] = useState(false);
  const [shipCarrier, setShipCarrier] = useState('顺丰');
  const [shipTracking, setShipTracking] = useState('');
  const [currentShipOrder, setCurrentShipOrder] = useState<SalesOrderDto | null>(null);
  const [partialItems, setPartialItems] = useState<Record<number, number>>({});
  const [partialDetail, setPartialDetail] = useState<OrderDetailResp | null>(null);
  const [inventories, setInventories] = useState<InventoryDto[]>([]);
  const [loadingInventories, setLoadingInventories] = useState(false);
  const [oosRecords, setOosRecords] = useState<OutOfStockRecordDto[]>([]);
  const [loadingOos, setLoadingOos] = useState(false);
  const [customerOos, setCustomerOos] = useState<CustomerOosRequestDto[]>([]);
  const [loadingCustomerOos, setLoadingCustomerOos] = useState(false);
  const [purchaseOrders, setPurchaseOrders] = useState<PurchaseOrderDto[]>([]);
  const [loadingPo, setLoadingPo] = useState(false);
  const [poModalVisible, setPoModalVisible] = useState(false);
  const [activePo, setActivePo] = useState<PurchaseOrderDto | null>(null);
  const [activePoItems, setActivePoItems] = useState<any[]>([]);
  const [createPoVisible, setCreatePoVisible] = useState(false);
  const [selectedOosIds, setSelectedOosIds] = useState<number[]>([]);
  const [poSupplierId, setPoSupplierId] = useState<number | null>(null);
  const [poExpectedDate, setPoExpectedDate] = useState<string>('');
  const [poBuyer, setPoBuyer] = useState<string>('');
  const [customers, setCustomers] = useState<CustomerDto[]>([]);
  const [loadingCustomers, setLoadingCustomers] = useState(false);
  const [suppliers, setSuppliers] = useState<any[]>([]);
  const [loadingSuppliers, setLoadingSuppliers] = useState(false);
  const [showAddSupplier, setShowAddSupplier] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<any | null>(null);
  const [newSupplier, setNewSupplier] = useState({
    supplierName: '',
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    paymentTerms: ''
  });
  const [supplyListVisible, setSupplyListVisible] = useState(false);
  const [activeSupplier, setActiveSupplier] = useState<any | null>(null);
  const [supplyList, setSupplyList] = useState<any[]>([]);
  const [loadingSupplyList, setLoadingSupplyList] = useState(false);
  const [editingSupply, setEditingSupply] = useState<any | null>(null);
  const [showAddSupply, setShowAddSupply] = useState(false);
  const [newSupplyForSupplier, setNewSupplyForSupplier] = useState({
    bookId: '',
    supplyPrice: 0,
    leadTimeDays: undefined as number | undefined,
    primary: false
  });
  const [books, setBooks] = useState<BookDto[]>([]);
  const [loadingBooks, setLoadingBooks] = useState(false);
  const [showAddBook, setShowAddBook] = useState(false);
  const [newBook, setNewBook] = useState({
    bookId: '',
    isbn: '',
    title: '',
    publisher: '',
    price: 0,
    coverImageUrl: '',
    catalog: '',
    initQuantity: 0,
    safetyStock: 10,
    seriesFlag: false,
    parentBookId: ''
  });
  const [activeBook, setActiveBook] = useState<BookDto | null>(null);
  const [bookDetailVisible, setBookDetailVisible] = useState(false);
  const [bookDetailSaving, setBookDetailSaving] = useState(false);
  const [editBook, setEditBook] = useState<any | null>(null);
  const [authors, setAuthors] = useState<AuthorDto[]>([]);
  const [keywords, setKeywords] = useState<KeywordDto[]>([]);
  const [supplies, setSupplies] = useState<SupplyDto[]>([]);
  const [loadingMeta, setLoadingMeta] = useState(false);
  const [newAuthor, setNewAuthor] = useState({
    authorName: '',
    nationality: '',
    authorOrder: 1
  });
  const [newKeyword, setNewKeyword] = useState({
    keywordText: ''
  });
  const [newSupply, setNewSupply] = useState({
    supplierId: undefined as number | undefined,
    supplyPrice: undefined as number | undefined,
    leadTimeDays: undefined as number | undefined,
    primary: false
  });
  const [showAddOos, setShowAddOos] = useState(false);
  const [newOos, setNewOos] = useState({
    bookId: '',
    requiredQuantity: 1,
    priority: 1
  });

  // ... imports and interfaces remain same ...

  // Sync selectedKey to URL hash for persistence across refresh
  useEffect(() => {
    window.location.hash = selectedKey;
  }, [selectedKey]);

  useEffect(() => {
    // Dashboard needs multiple data sources for stats
    if (selectedKey === 'dashboard') {
      loadOrders('全部');
      loadInventories();
      loadCustomers();
      loadBooks();
    }
    if (selectedKey === 'orders') {
      loadOrders(orderStatusFilter);
    }
    if (selectedKey === 'shipments') {
      loadShipmentOrders(shipmentStatusFilter);
    }
    if (selectedKey === 'inventory') {
      loadInventories();
    }
    if (selectedKey === 'purchase') {
      loadPurchaseData();
    }
    if (selectedKey === 'customer') {
      loadCustomers();
    }
    if (selectedKey === 'supplier') {
      loadSuppliers();
    } else if (selectedKey === 'book') {
      loadBooks();
    } else if (selectedKey === 'inquiry') {
      loadInquiries();
    }
  }, [selectedKey, orderStatusFilter, shipmentStatusFilter]);

  // --- Render Helpers ---

  // --- Render Helpers ---

  const loadOrders = async (status: string) => {
    try {
      setLoadingOrders(true);
      const resp = await http.get<SalesOrderDto[]>('/admin/orders', {
        params: { status }
      });
      setOrders(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单列表失败');
    } finally {
      setLoadingOrders(false);
    }
  };

  const loadShipmentOrders = async (status: string) => {
    try {
      setLoadingShipOrders(true);
      const resp = await http.get<SalesOrderDto[]>('/admin/orders', {
        params: { status }
      });
      setShipmentOrders(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载待发货订单失败');
    } finally {
      setLoadingShipOrders(false);
    }
  };

  const openOrderDetail = async (orderId: number) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/admin/orders/${orderId}`);
      setActiveOrderDetail(resp.data);
      setOrderDetailVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单详情失败');
    }
  };

  const openShipModal = async (order: SalesOrderDto) => {
    // 检查订单是否已经有shipment记录（分次发货后不能再整单发货）
    try {
      const resp = await http.get<OrderDetailResp>(`/admin/orders/${order.orderId}`);
      if (resp.data.shipments && resp.data.shipments.length > 0) {
        message.warning('该订单已经进行过分次发货，不能再进行整单发货。请继续使用分次发货功能完成剩余商品的发货。');
        return;
      }
    } catch (e: any) {
      message.error('检查订单信息失败');
      return;
    }
    
    setCurrentShipOrder(order);
    setShipCarrier('顺丰');
    setShipTracking('');
    setShipModalVisible(true);
  };

  const submitShip = async () => {
    if (!currentShipOrder) return;
    if (!shipCarrier.trim() || !shipTracking.trim()) {
      message.warning('请填写快递公司和运单号');
      return;
    }
    try {
      await http.post(`/admin/orders/${currentShipOrder.orderId}/ship`, {
        carrier: shipCarrier,
        trackingNumber: shipTracking,
        operator: adminName
      });
      message.success('发货成功');
      setShipModalVisible(false);
      await loadShipmentOrders(shipmentStatusFilter);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '发货失败');
    }
  };

  const openPartialModal = async (order: SalesOrderDto) => {
    try {
      const resp = await http.get<OrderDetailResp>(`/admin/orders/${order.orderId}`);
      const detail = resp.data;
      const remainMap: Record<number, number> = {};
      detail.items.forEach((it) => {
        const shipped = it.shippedQuantity ?? 0;
        const remain = it.quantity - shipped;
        if (remain > 0) {
          remainMap[it.orderItemId] = remain;
        }
      });
      setPartialDetail(detail);
      setPartialItems(remainMap);
      setCurrentShipOrder(order);
      setShipCarrier('顺丰');
      setShipTracking('');
      setPartialModalVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载订单明细失败');
    }
  };

  const submitPartial = async () => {
    if (!currentShipOrder || !partialDetail) return;
    if (!shipCarrier.trim() || !shipTracking.trim()) {
      message.warning('请填写快递公司和运单号');
      return;
    }
    const payloadItems = partialDetail.items
      .map((it) => {
        const qty = partialItems[it.orderItemId] ?? 0;
        return { orderItemId: it.orderItemId, shipQuantity: qty };
      })
      .filter((it) => it.shipQuantity > 0);
    if (payloadItems.length === 0) {
      message.warning('请为至少一条明细填写发货数量');
      return;
    }
    try {
      await http.post(`/admin/orders/${currentShipOrder.orderId}/ship/partial`, {
        carrier: shipCarrier,
        trackingNumber: shipTracking,
        operator: adminName,
        items: payloadItems
      });
      message.success('分次发货成功');
      setPartialModalVisible(false);
      await loadShipmentOrders(shipmentStatusFilter);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '分次发货失败');
    }
  };

  const loadInventories = async () => {
    try {
      setLoadingInventories(true);
      const resp = await http.get<InventoryDto[]>('/admin/inventory');
      setInventories(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载库存信息失败');
    } finally {
      setLoadingInventories(false);
    }
  };

  const updateSafetyStock = async (bookId: string, value: number | null | undefined) => {
    if (value == null || value < 0) {
      message.warning('安全库存必须是非负整数');
      return;
    }
    try {
      await http.post(`/admin/inventory/${bookId}/safety-stock`, {
        safetyStock: value
      });
      message.success('安全库存已更新');
      await loadInventories();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '更新安全库存失败');
    }
  };

  const adjustInventory = async (bookId: string, delta: number) => {
    try {
      await http.post(`/admin/inventory/${bookId}/adjust`, { delta });
      message.success(delta > 0 ? '库存增加成功' : '库存减少成功');
      await loadInventories();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '调整库存失败');
    }
  };

  const loadPurchaseData = async () => {
    try {
      setLoadingOos(true);
      setLoadingCustomerOos(true);
      setLoadingPo(true);
      const [oosResp, customerResp, poResp] = await Promise.all([
        http.get<OutOfStockRecordDto[]>('/admin/purchase/out-of-stock'),
        http.get<CustomerOosRequestDto[]>('/admin/purchase/customer-requests'),
        http.get<PurchaseOrderDto[]>('/admin/purchase/orders')
      ]);
      setOosRecords(oosResp.data);
      setCustomerOos(customerResp.data);
      setPurchaseOrders(poResp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载采购数据失败');
    } finally {
      setLoadingOos(false);
      setLoadingCustomerOos(false);
      setLoadingPo(false);
    }
  };

  const handleCustomerOos = async (record: CustomerOosRequestDto, action: 'accept' | 'reject') => {
    try {
      if (action === 'accept') {
        await http.post(`/admin/purchase/customer-requests/${record.requestId}/accept`);
        message.success('已生成缺书记录');
      } else {
        await http.post(`/admin/purchase/customer-requests/${record.requestId}/reject`);
        message.success('已标记为不生成缺书记录');
      }
      await loadPurchaseData();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '处理失败');
    }
  };

  const openCreatePoModal = () => {
    if (!selectedOosIds.length) {
      message.warning('请先在缺书记录中勾选至少一条记录');
      return;
    }
    setPoSupplierId(null);
    setPoExpectedDate('');
    setPoBuyer(adminName);
    setCreatePoVisible(true);
  };

  const submitCreatePo = async () => {
    if (!selectedOosIds.length) {
      message.warning('请选择缺书记录');
      return;
    }
    if (!poSupplierId) {
      message.warning('请输入供应商ID');
      return;
    }
    try {
      const resp = await http.post<PurchaseOrderDto>('/admin/purchase/orders/from-out-of-stock', {
        recordIds: selectedOosIds,
        supplierId: poSupplierId,
        expectedDate: poExpectedDate || null,
        buyer: poBuyer
      });
      message.success('采购单创建成功');
      setCreatePoVisible(false);
      setSelectedOosIds([]);
      await loadPurchaseData();
      setActivePo(resp.data);
      // 加载明细
      const detail = await http.get<any>(`/admin/purchase/orders/${resp.data.purchaseOrderId}`);
      setActivePoItems(detail.data.items || []);
      setPoModalVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '创建采购单失败');
    }
  };

  const submitAddOos = async () => {
    if (!newOos.bookId.trim()) {
      message.warning('请填写书号');
      return;
    }
    if (!newOos.requiredQuantity || newOos.requiredQuantity <= 0) {
      message.warning('需求数量必须为正整数');
      return;
    }
    try {
      await http.post('/admin/purchase/out-of-stock', {
        bookId: newOos.bookId.trim(),
        requiredQuantity: newOos.requiredQuantity,
        priority: newOos.priority || 1
      });
      message.success('缺书记录已添加');
      setShowAddOos(false);
      setNewOos({
        bookId: '',
        requiredQuantity: 1,
        priority: 1
      });
      await loadPurchaseData();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加缺书记录失败');
    }
  };

  const openPoDetail = async (po: PurchaseOrderDto) => {
    try {
      const resp = await http.get<any>(`/admin/purchase/orders/${po.purchaseOrderId}`);
      setActivePo(po);
      setActivePoItems(resp.data.items || []);
      setPoModalVisible(true);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载采购单明细失败');
    }
  };

  const receivePurchase = async (po: PurchaseOrderDto) => {
    try {
      await http.post(`/admin/purchase/orders/${po.purchaseOrderId}/receive`);
      message.success('到货处理完成，库存已更新');
      await loadPurchaseData();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '到货处理失败');
    }
  };

  const loadCustomers = async () => {
    try {
      setLoadingCustomers(true);
      const resp = await http.get<CustomerDto[]>('/admin/customers');
      setCustomers(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载客户列表失败');
    } finally {
      setLoadingCustomers(false);
    }
  };

  const updateCustomerCredit = async (record: CustomerDto, level: number) => {
    try {
      await http.post(`/admin/customers/${record.customerId}/credit-level`, {
        creditLevelId: level
      });
      message.success(`信用等级已调整为 ${level} 级`);
      await loadCustomers();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '调整信用等级失败');
    }
  };

  const loadSuppliers = async () => {
    try {
      setLoadingSuppliers(true);
      const resp = await http.get<any[]>('/admin/suppliers');
      setSuppliers(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载供应商列表失败');
    } finally {
      setLoadingSuppliers(false);
    }
  };

  const submitAddSupplier = async () => {
    if (!newSupplier.supplierName.trim()) {
      message.warning('请填写供应商名称');
      return;
    }
    try {
      if (editingSupplier) {
        // 编辑模式
        await http.put(`/admin/suppliers/${editingSupplier.supplierId}`, newSupplier);
        message.success('供应商已更新');
      } else {
        // 添加模式
        await http.post('/admin/suppliers', newSupplier);
        message.success('供应商已添加');
      }
      setShowAddSupplier(false);
      setEditingSupplier(null);
      setNewSupplier({
        supplierName: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        paymentTerms: ''
      });
      await loadSuppliers();
    } catch (e: any) {
      message.error(e?.response?.data?.message || (editingSupplier ? '更新供应商失败' : '添加供应商失败'));
    }
  };

  const openEditSupplier = (supplier: any) => {
    setEditingSupplier(supplier);
    setNewSupplier({
      supplierName: supplier.supplierName || '',
      contactPerson: supplier.contactPerson || '',
      phone: supplier.phone || '',
      email: supplier.email || '',
      address: supplier.address || '',
      paymentTerms: supplier.paymentTerms || ''
    });
    setShowAddSupplier(true);
  };

  const deleteSupplier = async (supplierId: number) => {
    try {
      await http.delete(`/admin/suppliers/${supplierId}`);
      message.success('供应商已删除');
      await loadSuppliers();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除供应商失败');
    }
  };

  const openSupplyList = async (supplier: any) => {
    setActiveSupplier(supplier);
    setSupplyListVisible(true);
    await loadSupplyList(supplier.supplierId);
  };

  const loadSupplyList = async (supplierId: number) => {
    try {
      setLoadingSupplyList(true);
      const resp = await http.get<any[]>(`/admin/suppliers/${supplierId}/supplies`);
      setSupplyList(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载供货清单失败');
    } finally {
      setLoadingSupplyList(false);
    }
  };

  const saveSupplyEdit = async (supply: any) => {
    if (!activeSupplier) return;
    try {
      await http.put(`/admin/suppliers/${activeSupplier.supplierId}/supplies/${supply.bookId}`, {
        supplyPrice: supply.supplyPrice,
        leadTimeDays: supply.leadTimeDays ?? null,
        primary: supply.primary
      });
      message.success('供货关系已更新');
      setEditingSupply(null);
      await loadSupplyList(activeSupplier.supplierId);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '更新供货关系失败');
    }
  };

  const deleteSupply = async (supply: any) => {
    if (!activeSupplier) return;
    try {
      await http.delete(`/admin/suppliers/${activeSupplier.supplierId}/supplies/${supply.bookId}`);
      message.success('供货关系已删除');
      await loadSupplyList(activeSupplier.supplierId);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除供货关系失败');
    }
  };

  const submitAddSupplyForSupplier = async () => {
    if (!activeSupplier) return;
    if (!newSupplyForSupplier.bookId.trim()) {
      message.warning('请输入书号');
      return;
    }
    try {
      await http.post(`/admin/suppliers/${activeSupplier.supplierId}/supplies`, {
        bookId: newSupplyForSupplier.bookId.trim(),
        supplyPrice: newSupplyForSupplier.supplyPrice || null,
        leadTimeDays: newSupplyForSupplier.leadTimeDays || null,
        primary: newSupplyForSupplier.primary
      });
      message.success('供货关系已添加');
      setShowAddSupply(false);
      setNewSupplyForSupplier({
        bookId: '',
        supplyPrice: 0,
        leadTimeDays: undefined,
        primary: false
      });
      await loadSupplyList(activeSupplier.supplierId);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加供货关系失败');
    }
  };

  const loadBooks = async () => {
    try {
      setLoadingBooks(true);
      const resp = await http.get<BookDto[]>('/admin/books');
      setBooks(resp.data);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载书目失败');
    } finally {
      setLoadingBooks(false);
    }
  };

  const submitAddBook = async () => {
    if (!newBook.bookId.trim()) {
      message.warning('请填写书号');
      return;
    }
    if (!newBook.title.trim()) {
      message.warning('请填写书名');
      return;
    }
    if (newBook.price < 0) {
      message.warning('价格必须为非负数');
      return;
    }
    if (newBook.initQuantity < 0 || newBook.safetyStock < 0) {
      message.warning('初始库存和安全库存必须为非负整数');
      return;
    }
    try {
      await http.post('/admin/books', {
        bookId: newBook.bookId.trim(),
        isbn: newBook.isbn.trim() || null,
        title: newBook.title.trim(),
        publisher: newBook.publisher.trim() || null,
        price: newBook.price,
        coverImageUrl: newBook.coverImageUrl.trim() || null,
        catalog: newBook.catalog.trim() || null,
        initQuantity: newBook.initQuantity,
        safetyStock: newBook.safetyStock,
        seriesFlag: newBook.seriesFlag,
        parentBookId: (newBook.parentBookId && newBook.parentBookId !== '_pending_') ? newBook.parentBookId.trim() : null
      });
      message.success('书目已添加');
      setShowAddBook(false);
      setNewBook({
        bookId: '',
        isbn: '',
        title: '',
        publisher: '',
        price: 0,
        coverImageUrl: '',
        catalog: '',
        initQuantity: 0,
        safetyStock: 10,
        seriesFlag: false,
        parentBookId: ''
      });
      await loadBooks();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加书目失败');
    }
  };

  const openBookDetail = async (record: BookDto) => {
    try {
      setActiveBook(record);
      setBookDetailVisible(true);
      setLoadingMeta(true);
      // 加载详情与元数据
      const [detailResp, authorsResp, keywordsResp, suppliesResp] = await Promise.all([
        http.get<any>(`/admin/books/${record.bookId}`),
        http.get<AuthorDto[]>(`/admin/books/${record.bookId}/authors`),
        http.get<KeywordDto[]>(`/admin/books/${record.bookId}/keywords`),
        http.get<SupplyDto[]>(`/admin/books/${record.bookId}/supplies`)
      ]);
      const book = detailResp.data.book || record;
      setEditBook({
        isbn: book.isbn || '',
        title: book.title || '',
        publisher: book.publisher || '',
        edition: book.edition || '',
        price: book.price ?? 0,
        status: book.status || 'AVAILABLE',
        coverImageUrl: book.coverImageUrl || '',
        catalog: book.catalog || ''
      });
      setAuthors(authorsResp.data || []);
      setKeywords(keywordsResp.data || []);
      setSupplies(suppliesResp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '加载书目详情失败');
    } finally {
      setLoadingMeta(false);
    }
  };

  const submitBookBaseInfo = async () => {
    if (!activeBook || !editBook) return;
    if (!editBook.title.trim()) {
      message.warning('书名不能为空');
      return;
    }
    if (editBook.price < 0) {
      message.warning('价格必须为非负数');
      return;
    }
    try {
      setBookDetailSaving(true);
      await http.put(`/admin/books/${activeBook.bookId}`, {
        isbn: editBook.isbn || null,
        title: editBook.title.trim(),
        publisher: editBook.publisher || null,
        edition: editBook.edition || null,
        price: editBook.price,
        status: editBook.status || 'AVAILABLE',
        coverImageUrl: editBook.coverImageUrl || null,
        catalog: editBook.catalog || null
      });
      message.success('书目基本信息已保存');
      await loadBooks();
    } catch (e: any) {
      message.error(e?.response?.data?.message || '保存失败');
    } finally {
      setBookDetailSaving(false);
    }
  };

  const deleteBook = async (bookId: string) => {
    try {
      await http.delete(`/admin/books/${bookId}`);
      message.success('书目已删除');
      await loadBooks();
      // 如果删除的是当前查看的详情，关闭详情弹窗
      if (activeBook?.bookId === bookId) {
        setBookDetailVisible(false);
        setActiveBook(null);
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除书目失败');
    }
  };

  const addAuthor = async () => {
    if (!activeBook) return;
    if (!newAuthor.authorName.trim()) {
      message.warning('请输入作者姓名');
      return;
    }
    try {
      await http.post(`/admin/books/${activeBook.bookId}/authors`, {
        authorName: newAuthor.authorName.trim(),
        nationality: newAuthor.nationality || null,
        authorOrder: newAuthor.authorOrder || 1
      });
      message.success('作者已添加');
      setNewAuthor({ authorName: '', nationality: '', authorOrder: 1 });
      const resp = await http.get<AuthorDto[]>(`/admin/books/${activeBook.bookId}/authors`);
      setAuthors(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加作者失败');
    }
  };

  const updateAuthorOrder = async (row: AuthorDto, order: number) => {
    if (!activeBook) return;
    try {
      await http.put(`/admin/books/${activeBook.bookId}/authors/${row.authorId}`, {
        authorOrder: order
      });
      const resp = await http.get<AuthorDto[]>(`/admin/books/${activeBook.bookId}/authors`);
      setAuthors(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '更新作者顺序失败');
    }
  };

  const removeAuthor = async (row: AuthorDto) => {
    if (!activeBook) return;
    try {
      await http.delete(`/admin/books/${activeBook.bookId}/authors/${row.authorId}`);
      message.success('作者已移除');
      const resp = await http.get<AuthorDto[]>(`/admin/books/${activeBook.bookId}/authors`);
      setAuthors(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '移除作者失败');
    }
  };

  const addKeyword = async () => {
    if (!activeBook) return;
    if (!newKeyword.keywordText.trim()) {
      message.warning('请输入关键字');
      return;
    }
    try {
      await http.post(`/admin/books/${activeBook.bookId}/keywords`, {
        keywordText: newKeyword.keywordText.trim()
      });
      message.success('关键字已添加');
      setNewKeyword({ keywordText: '' });
      const resp = await http.get<KeywordDto[]>(`/admin/books/${activeBook.bookId}/keywords`);
      setKeywords(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加关键字失败');
    }
  };

  const removeKeyword = async (row: KeywordDto) => {
    if (!activeBook) return;
    try {
      await http.delete(`/admin/books/${activeBook.bookId}/keywords/${row.keywordId}`);
      message.success('关键字已移除');
      const resp = await http.get<KeywordDto[]>(`/admin/books/${activeBook.bookId}/keywords`);
      setKeywords(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '移除关键字失败');
    }
  };

  const addSupply = async () => {
    if (!activeBook) return;
    if (!newSupply.supplierId) {
      message.warning('请输入供应商ID');
      return;
    }
    try {
      await http.post(`/admin/books/${activeBook.bookId}/supplies`, {
        supplierId: newSupply.supplierId,
        supplyPrice: newSupply.supplyPrice ?? null,
        leadTimeDays: newSupply.leadTimeDays ?? null,
        primary: newSupply.primary
      });
      message.success('供货关系已添加');
      setNewSupply({
        supplierId: undefined,
        supplyPrice: undefined,
        leadTimeDays: undefined,
        primary: false
      });
      const resp = await http.get<SupplyDto[]>(`/admin/books/${activeBook.bookId}/supplies`);
      setSupplies(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '添加供货关系失败');
    }
  };

  const removeSupply = async (row: SupplyDto) => {
    if (!activeBook) return;
    try {
      await http.delete(`/admin/books/${activeBook.bookId}/supplies/${row.supplierId}`);
      message.success('供货关系已删除');
      const resp = await http.get<SupplyDto[]>(`/admin/books/${activeBook.bookId}/supplies`);
      setSupplies(resp.data || []);
    } catch (e: any) {
      message.error(e?.response?.data?.message || '删除供货关系失败');
    }
  };

  const orderColumns: ColumnsType<SalesOrderDto> = [
    {
      title: '订单号',
      dataIndex: 'orderId',
      render: (val, record) => (
        <a onClick={() => openOrderDetail(record.orderId)}>{val}</a>
      )
    },
    {
      title: '客户ID',
      dataIndex: 'customerId'
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
    }
  ];

  // 订单状态中文映射
  const orderStatusMap: Record<string, string> = {
    PENDING_PAYMENT: '待付款',
    OUT_OF_STOCK_PENDING: '缺货待确认',
    PENDING_SHIPMENT: '待发货',
    DELIVERING: '配送中',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  };

  // 书目状态中文映射
  const bookStatusMap: Record<string, string> = {
    AVAILABLE: '在售',
    OUT_OF_STOCK: '缺货',
    DISCONTINUED: '已下架'
  };

  // 缺书记录状态中文映射
  const oosStatusMap: Record<string, string> = {
    PENDING: '待处理',
    IN_PURCHASE: '采购中',
    RESOLVED: '已解决'
  };

  // 采购单状态中文映射
  const purchaseStatusMap: Record<string, string> = {
    PENDING: '待发货',
    ISSUED: '已下单',
    SHIPPED: '已发货',
    COMPLETED: '已到货'
  };

  // 发货单状态中文映射
  const shipmentStatusMap: Record<string, string> = {
    SHIPPED: '已发货',
    IN_TRANSIT: '配送中',
    DELIVERED: '已送达',
    RECEIVED: '已签收'
  };

  // 订单明细状态中文映射
  const itemStatusMap: Record<string, string> = {
    ORDERED: '已下单',
    SHIPPED: '已发货',
    PARTIAL_SHIPPED: '部分发货',
    RECEIVED: '已收货',
    CANCELLED: '已取消'
  };


  const renderOrders = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4} style={{ margin: 0 }}>订单列表</Title>
        <div className="glass-panel" style={{ padding: '4px 12px', borderRadius: 8 }}>
          <Space align="center">
            <span>状态筛选：</span>
            <Select
              variant="borderless"
              style={{ width: 220, fontWeight: 500 }}
              value={orderStatusFilter}
              onChange={(val) => setOrderStatusFilter(val)}
              options={[
                { label: '全部订单', value: '全部' },
                { label: '待付款', value: 'PENDING_PAYMENT' },
                { label: '缺货待确认', value: 'OUT_OF_STOCK_PENDING' },
                { label: '待发货', value: 'PENDING_SHIPMENT' },
                { label: '配送中', value: 'DELIVERING' },
                { label: '已发货', value: 'SHIPPED' },
                { label: '已完成', value: 'COMPLETED' },
                { label: '已取消', value: 'CANCELLED' }
              ]}
            />
          </Space>
        </div>
      </div>
      <Table<SalesOrderDto>
        rowKey="orderId"
        size="middle"
        columns={[
          { title: '订单号', dataIndex: 'orderId', width: 80 },
          { title: '顾客ID', dataIndex: 'customerId', width: 80 },
          { title: '下单时间', dataIndex: 'orderTime', width: 180 },
          {
            title: '状态',
            dataIndex: 'orderStatus',
            render: (v: string) => {
              let color = 'default';
              if (v === 'PENDING_PAYMENT') color = 'warning';
              if (v === 'PENDING_SHIPMENT') color = 'processing';
              if (v === 'COMPLETED') color = 'success';
              if (v === 'CANCELLED') color = 'error';
              return <Tag color={color}>{orderStatusMap[v] || v}</Tag>;
            }
          },
          {
            title: '金额',
            dataIndex: 'payableAmount',
            align: 'right',
            render: (v: number) => <Text strong>¥{v?.toFixed(2)}</Text>
          },
          {
            title: '发货地址',
            dataIndex: 'shippingAddressSnapshot',
            ellipsis: true,
            width: 200,
            render: (v: string) => v || '-'
          },
          {
            title: '操作',
            key: 'action',
            render: (_, record) => (
              <Button type="link" size="small" onClick={() => openOrderDetail(record.orderId)}>
                查看详情
              </Button>
            )
          }
        ]}
        dataSource={orders}
        loading={loadingOrders}
        pagination={{ pageSize: 12 }}
        className="glass-table"
      />
    </Space>
  );

  const renderShipments = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4} style={{ margin: 0 }}>发货处理</Title>
        <div className="glass-panel" style={{ padding: '4px 12px', borderRadius: 8 }}>
          <Select
            variant="borderless"
            style={{ width: 220 }}
            value={shipmentStatusFilter}
            onChange={(val) => setShipmentStatusFilter(val)}
            options={[
              { label: '全部记录', value: '全部' },
              { label: '待发货任务', value: 'PENDING_SHIPMENT' },
              { label: '配送中监控', value: 'DELIVERING' },
              { label: '赊销待发货', value: 'PENDING_PAYMENT' }
            ]}
          />
        </div>
      </div>
      <Table<SalesOrderDto>
        rowKey="orderId"
        size="middle"
        columns={[
          { title: '订单号', dataIndex: 'orderId', width: 90 },
          { title: '顾客ID', dataIndex: 'customerId', width: 90 },
          { title: '下单时间', dataIndex: 'orderTime' },
          { title: '状态', dataIndex: 'orderStatus', render: (v: string) => <Tag color="blue">{orderStatusMap[v] || v}</Tag> },
          {
            title: '操作',
            key: 'action',
            width: 220,
            render: (_, record) => {
              const canShip =
                record.orderStatus === 'PENDING_SHIPMENT' ||
                record.orderStatus === 'DELIVERING' ||
                record.orderStatus === 'PENDING_PAYMENT';
              return (
                <Space style={{ whiteSpace: 'nowrap' }}>
                  <a onClick={() => openOrderDetail(record.orderId)}>详情</a>
                  {canShip && (
                    <>
                      <a onClick={() => openShipModal(record)}>整单发货</a>
                      <a onClick={() => openPartialModal(record)}>分次发货</a>
                    </>
                  )}
                </Space>
              );
            }
          }
        ]}
        dataSource={
          shipmentStatusFilter === '全部'
            ? shipmentOrders
            : shipmentOrders.filter((o) => o.orderStatus === shipmentStatusFilter)
        }
        loading={loadingShipOrders}
        pagination={{ pageSize: 12 }}
        className="glass-table"
      />
    </Space>
  );

  const renderCustomers = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Title level={4}>客户列表</Title>
      <Table<CustomerDto>
        rowKey="customerId"
        size="middle"
        loading={loadingCustomers}
        dataSource={customers}
        pagination={{ pageSize: 12 }}
        className="glass-table"
        columns={[
          { title: 'ID', dataIndex: 'customerId', width: 80 },
          { title: '用户名', dataIndex: 'username', render: t => <Text strong>{t}</Text> },
          { title: '真实姓名', dataIndex: 'realName' },
          {
            title: '余额',
            dataIndex: 'accountBalance',
            render: (v: number) => (v != null ? `¥${v.toFixed(2)}` : '¥0.00')
          },
          {
            title: '累积消费',
            dataIndex: 'totalConsumption',
            render: (v: number) => (v != null ? `¥${v.toFixed(2)}` : '¥0.00')
          },
          { title: '信用等级', dataIndex: 'creditLevelId', render: v => <Tag color="gold">{v}级</Tag> },
          {
            title: '调整信用',
            key: 'action',
            render: (_, r) => (
              <Select
                size="small"
                value={r.creditLevelId}
                style={{ width: 100 }}
                onChange={(val) => updateCustomerCredit(r, val)}
                options={[1, 2, 3, 4, 5].map(i => ({ label: `${i}级`, value: i }))}
              />
            )
          }
        ]}
      />
    </Space>
  );

  const renderSuppliers = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4}>供应商库</Title>
        <Button type="primary" onClick={() => setShowAddSupplier(true)}>
          + 添加供应商
        </Button>
      </div>
      <Table<any>
        rowKey="supplierId"
        size="middle"
        loading={loadingSuppliers}
        dataSource={suppliers}
        pagination={{ pageSize: 12 }}
        className="glass-table"
        columns={[
          { title: 'ID', dataIndex: 'supplierId', width: 80 },
          { title: '名称', dataIndex: 'supplierName', render: t => <Text strong>{t}</Text> },
          { title: '联系人', dataIndex: 'contactPerson' },
          { title: '电话', dataIndex: 'phone' },
          { title: '邮箱', dataIndex: 'email' },
          { 
            title: '操作', 
            key: 'action', 
            width: 200,
            render: (_, record) => (
              <Space>
                <Button type="link" size="small" onClick={() => openSupplyList(record)}>供货清单</Button>
                <Button type="link" size="small" onClick={() => openEditSupplier(record)}>编辑</Button>
                <Popconfirm
                  title="确定要删除这个供应商吗？"
                  description="如果该供应商存在供货关系，将无法删除。请先删除所有供货关系后再删除供应商。此操作不可恢复。"
                  onConfirm={() => deleteSupplier(record.supplierId)}
                  okText="确定"
                  cancelText="取消"
                >
                  <Button type="link" size="small" danger>删除</Button>
                </Popconfirm>
              </Space>
            )
          }
        ]}
      />
    </Space>
  );

  const renderInventory = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Title level={4}>库存概览</Title>
      <Table<InventoryDto>
        rowKey="bookId"
        size="middle"
        dataSource={inventories}
        loading={loadingInventories}
        pagination={{ pageSize: 15 }}
        className="glass-table"
        columns={[
          { title: '书号', dataIndex: 'bookId', width: 150 },
          { 
            title: '当前库存', 
            dataIndex: 'quantity', 
            render: (v: number, record: InventoryDto) => (
              <Text 
                type={v < (record.safetyStock || 0) ? 'danger' : undefined} 
                strong 
                style={{ fontSize: 16 }}
              >
                {v}
              </Text>
            )
          },
          {
            title: '安全库存',
            dataIndex: 'safetyStock',
            render: (v: number, record) => (
              <InputNumber
                size="small"
                min={0}
                value={v}
                onChange={(val) => {
                  setInventories((prev) =>
                    prev.map((it) =>
                      it.bookId === record.bookId ? { ...it, safetyStock: Number(val ?? 0) } : it
                    )
                  );
                }}
                onBlur={() => updateSafetyStock(record.bookId, record.safetyStock)}
              />
            )
          },
          {
            title: '快速调整',
            key: 'action',
            render: (_, record) => (
              <Space>
                <Button size="small" onClick={() => adjustInventory(record.bookId, 10)}>+10</Button>
                <Button size="small" onClick={() => adjustInventory(record.bookId, -10)}>-10</Button>
              </Space>
            )
          }
        ]}
      />
    </Space>
  );

  const renderBooks = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Title level={4}>书目管理</Title>
        <Button type="primary" onClick={() => setShowAddBook(true)}>
          + 新增书目
        </Button>
      </div>
      <Table<BookDto>
        rowKey="bookId"
        size="middle"
        loading={loadingBooks}
        dataSource={books}
        pagination={{ pageSize: 12 }}
        className="glass-table"
        columns={[
          {
            title: '书号',
            dataIndex: 'bookId',
            width: 120,
            render: (v, record) => <a onClick={() => openBookDetail(record)} style={{ fontWeight: 500 }}>{v}</a>
          },
          {
            title: '类型',
            dataIndex: 'seriesFlag',
            width: 80,
            render: (_, record) => {
              if (record.seriesFlag) return <Tag color="purple">📚 丛书</Tag>;
              if (record.parentBookId) return <Tag color="blue">  └ 子书</Tag>;
              return <Tag color="default">普通</Tag>;
            }
          },
          {
            title: '封面',
            dataIndex: 'coverImageUrl',
            width: 60,
            render: (url) => url ? <img src={url} alt="cover" style={{ width: 30, height: 40, objectFit: 'cover', borderRadius: 4 }} /> : <div style={{ width: 30, height: 40, background: '#eee', borderRadius: 4 }} />
          },
          {
            title: '书名', dataIndex: 'title', render: (v, record) => (
              <span>
                {v}
                {record.parentBookId && <Text type="secondary" style={{ fontSize: 11, marginLeft: 6 }}>← {record.parentBookId}</Text>}
              </span>
            )
          },
          { title: '出版社', dataIndex: 'publisher' },
          { title: '定价', dataIndex: 'price', render: (v) => `¥${v.toFixed(2)}` },
          { title: '状态', dataIndex: 'status', render: (v: string) => <Tag color={v === 'AVAILABLE' ? 'success' : v === 'OUT_OF_STOCK' ? 'warning' : 'default'}>{bookStatusMap[v] || v}</Tag> },
          {
            title: '操作',
            key: 'action',
            width: 150,
            render: (_: any, record: BookDto) => {
              // 子书不允许删除
              if (record.parentBookId) {
                return (
                  <Popconfirm
                    title="提示"
                    description="不能直接删除子书，请删除其父丛书。"
                    okText="知道了"
                    cancelButtonProps={{ style: { display: 'none' } }}
                  >
                    <Button type="link" size="small" disabled>删除</Button>
                  </Popconfirm>
                );
              }
              
              // 丛书和普通书可以删除
              const isSeries = record.seriesFlag;
              return (
                <Popconfirm
                  title={isSeries ? "确定要删除这个丛书吗？" : "确定要删除这本书吗？"}
                  description={isSeries 
                    ? "删除丛书将同时删除所有子书及其供货关系，此操作不可恢复。" 
                    : "删除书籍将同时删除所有相关的供货关系，此操作不可恢复。"}
                  onConfirm={() => deleteBook(record.bookId)}
                  okText="确定"
                  cancelText="取消"
                >
                  <Button type="link" size="small" danger>删除</Button>
                </Popconfirm>
              );
            }
          }
        ]}
      />
    </Space>
  );

  const renderPurchase = () => (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Title level={4}>采购中心</Title>
      <div className="glass-panel" style={{ padding: 16, marginBottom: 16 }}>
        <Space style={{ width: '100%', justifyContent: 'space-between', marginBottom: 16 }}>
          <Text strong style={{ fontSize: 16 }}>缺书记录</Text>
          <Space>
            <Button onClick={() => setShowAddOos(true)}>手工登记</Button>
            <Button type="primary" onClick={openCreatePoModal} disabled={selectedOosIds.length === 0}>
              生成采购单 ({selectedOosIds.length})
            </Button>
          </Space>
        </Space>
        <Table<OutOfStockRecordDto>
          rowKey="recordId"
          size="small"
          loading={loadingOos}
          rowSelection={{
            selectedRowKeys: selectedOosIds,
            onChange: (keys) => setSelectedOosIds(keys as number[])
          }}
          dataSource={oosRecords}
          pagination={{ pageSize: 5 }}
          className="glass-table"
          columns={[
            { title: '书号', dataIndex: 'bookId' },
            { title: '缺货量', dataIndex: 'requiredQuantity' },
            { title: '状态', dataIndex: 'status', render: (v: string) => <Tag color={v === 'PENDING' ? 'warning' : v === 'IN_PURCHASE' ? 'processing' : 'success'}>{oosStatusMap[v] || v}</Tag> }
          ]}
        />
      </div>

      <div className="glass-panel" style={{ padding: 16, marginBottom: 16 }}>
        <Title level={5}>顾客缺货登记审核</Title>
        <Table<CustomerOosRequestDto>
          rowKey="requestId"
          size="small"
          loading={loadingCustomerOos}
          dataSource={customerOos}
          pagination={{ pageSize: 5 }}
          className="glass-table"
          columns={[
            { title: '订单号', dataIndex: 'orderId' },
            { title: '书号', dataIndex: 'bookId' },
            { title: '数量', dataIndex: 'requestedQty' },
            { title: '备注', dataIndex: 'customerNote' },
            {
              title: '操作',
              key: 'action',
              render: (_, r) => (
                <Space>
                  <Button type="link" size="small" onClick={() => handleCustomerOos(r, 'accept')}>同意</Button>
                  <Button type="text" danger size="small" onClick={() => handleCustomerOos(r, 'reject')}>拒绝</Button>
                </Space>
              )
            }
          ]}
        />
      </div>

      <div className="glass-panel" style={{ padding: 16 }}>
        <Title level={5}>采购单历史</Title>
        <Table<PurchaseOrderDto>
          rowKey="purchaseOrderId"
          size="small"
          loading={loadingPo}
          dataSource={purchaseOrders}
          pagination={{ pageSize: 8 }}
          className="glass-table"
          columns={[
            { title: '采购单号', dataIndex: 'purchaseOrderId', render: (v, r) => <a onClick={() => openPoDetail(r)}>{v}</a> },
            { title: '供应商', dataIndex: 'supplierId' },
            { title: '状态', dataIndex: 'status', render: (v: string) => <Tag color={v === 'COMPLETED' ? 'success' : 'processing'}>{purchaseStatusMap[v] || v}</Tag> },
            { title: '预估金额', dataIndex: 'estimatedAmount', render: v => `¥${v?.toFixed(2) || '-'}` },
            {
              title: '操作',
              render: (_, r) => (
                <Space>
                  <Button type="link" size="small" onClick={() => openPoDetail(r)}>明细</Button>
                  <Popconfirm title="确认到货？" onConfirm={() => receivePurchase(r)} disabled={r.status === 'COMPLETED'}>
                    <Button type="link" size="small" disabled={r.status === 'COMPLETED'}>确认到货</Button>
                  </Popconfirm>
                </Space>
              )
            }
          ]}
        />
      </div>
    </Space>
  );

  // --- Render Helpers ---

  const renderDashboard = () => {
    // Calculate simple stats
    const pendingOrders = orders.filter(o => o.orderStatus === 'PENDING_SHIPMENT').length;
    const lowStockBooks = inventories.filter(i => i.quantity < i.safetyStock).length;
    const totalSales = orders.filter(o => o.orderStatus !== 'CANCELLED').reduce((acc, cur) => acc + cur.payableAmount, 0);

    // Calculate sales data for the past 7 days from real orders
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    const data = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(today);
      date.setDate(today.getDate() - (6 - i)); // From 6 days ago to today
      const dateStr = `${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')}`;

      // Filter orders for this specific day
      const dayStart = new Date(date);
      dayStart.setHours(0, 0, 0, 0);
      const dayEnd = new Date(date);
      dayEnd.setHours(23, 59, 59, 999);

      const daySales = orders
        .filter(o => o.orderStatus !== 'CANCELLED')
        .filter(o => {
          const orderDate = new Date(o.orderTime);
          return orderDate >= dayStart && orderDate <= dayEnd;
        })
        .reduce((sum, o) => sum + o.payableAmount, 0);

      return { name: dateStr, sales: Math.round(daySales * 100) / 100 };
    });

    return (
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <Title level={4} style={{ margin: 0 }}>工作台概览</Title>
            <Text type="secondary">欢迎回来，今日业务概况如下</Text>
          </div>
          <Tag color="geekblue" style={{ fontSize: 14, padding: '4px 12px' }}>{new Date().toLocaleDateString()}</Tag>
        </div>

        <Row gutter={[24, 24]}>
          <Col span={6}>
            <Card className="liquid-glass shimmer-card" bordered={false}>
              <Statistic
                title={<span style={{ color: '#64748b' }}>总销售额</span>}
                value={totalSales}
                precision={2}
                formatter={(val) => <span className="text-gradient-indigo">¥ {Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>}
                prefix=""
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card className="liquid-glass shimmer-card" bordered={false}>
              <Statistic
                title={<span style={{ color: '#64748b' }}>待发货订单</span>}
                value={pendingOrders}
                valueStyle={{ color: '#0ea5e9', fontWeight: 600 }}
                prefix={<ShoppingOutlined />}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card className="liquid-glass shimmer-card" bordered={false}>
              <Statistic
                title={<span style={{ color: '#64748b' }}>库存预警</span>}
                value={lowStockBooks}
                valueStyle={{ color: '#f59e0b', fontWeight: 600 }}
                prefix={<ArrowDownOutlined />}
                suffix="种"
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card className="glass-card" bordered={false}>
              <Statistic
                title={<span style={{ color: '#64748b' }}>用户/书目</span>}
                value={`${customers.length} / ${books.length}`}
                valueStyle={{ color: '#6366f1', fontWeight: 600 }}
                prefix={<TeamOutlined />}
              />
            </Card>
          </Col>
        </Row>

        {/* Charts Section */}
        <Row gutter={[24, 24]}>
          <Col span={16}>
            <Card className="glass-card aurora-glow" title="近七日销售趋势" bordered={false}>
              <div style={{ height: 300, width: '100%' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={data} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#6366f1" stopOpacity={0.8} />
                        <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="name" tick={{ fill: '#64748b' }} axisLine={false} tickLine={false} />
                    <YAxis
                      tick={{ fill: '#64748b' }}
                      axisLine={false}
                      tickLine={false}
                      tickFormatter={(value) => {
                        if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`;
                        if (value >= 1000) return `${(value / 1000).toFixed(0)}K`;
                        return value;
                      }}
                      width={60}
                    />
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                    <Tooltip
                      contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                      itemStyle={{ color: '#6366f1' }}
                    />
                    <Area type="monotone" dataKey="sales" stroke="#6366f1" strokeWidth={3} fillOpacity={1} fill="url(#colorSales)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </Col>
          <Col span={8}>
            <div className="glass-panel" style={{ height: '100%', padding: 24, borderRadius: 12, display: 'flex', flexDirection: 'column' }}>
              <Title level={5} style={{ marginBottom: 24 }}>快捷操作</Title>
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Button block type="primary" size="large" icon={<ShoppingOutlined />} onClick={() => setSelectedKey('orders')} className="neon-btn" style={{ height: 48, borderRadius: 8 }}>处理订单</Button>
                <Button block size="large" icon={<CarOutlined />} onClick={() => setSelectedKey('shipments')} style={{ height: 48, borderRadius: 8 }}>发货管理</Button>
                <Button block size="large" icon={<AppstoreOutlined />} onClick={() => setSelectedKey('inventory')} style={{ height: 48, borderRadius: 8 }}>库存调整</Button>
                <Button block size="large" icon={<BookOutlined />} onClick={() => setShowAddBook(true)} style={{ height: 48, borderRadius: 8 }}>新增书籍</Button>
              </Space>
              <div style={{ marginTop: 'auto', paddingTop: 24 }}>
                <div style={{ background: 'rgba(99, 102, 241, 0.05)', padding: 16, borderRadius: 8, border: '1px solid rgba(99, 102, 241, 0.1)' }}>
                  <Text strong style={{ color: '#4f46e5' }}>NEW: 销售报表 beta</Text>
                  <br />
                  <Text type="secondary" style={{ fontSize: 12 }}>新的可视化图表已上线，数据更直观。</Text>
                </div>
              </div>
            </div>
          </Col>
        </Row>
      </Space>
    );
  };

  const renderContent = () => {
    switch (selectedKey) {
      case 'dashboard': return renderDashboard();
      case 'orders': return renderOrders();
      case 'shipments': return renderShipments();
      case 'inventory': return renderInventory();
      case 'purchase': return renderPurchase();
      case 'customer': return renderCustomers();
      case 'supplier': return renderSuppliers();
      case 'book': return renderBooks();
      case 'inquiry': return renderInquiries();
      default: return null;
    }
  };

  const renderInquiries = () => {
    // Filter logic
    const filteredInquiries = inquiries;

    const columns: ColumnsType<BookInquiryRequestDto> = [
      { title: 'ID', dataIndex: 'inquiryId', width: 80 },
      { title: '客户ID', dataIndex: 'customerId', width: 100 },
      { title: '书名', dataIndex: 'bookTitle' },
      { title: '数量', dataIndex: 'quantity', width: 80 },
      {
        title: '状态', dataIndex: 'status', width: 120, render: (s: string) => {
          const map: Record<string, any> = { 'PENDING': { text: '待处理', color: 'orange' }, 'QUOTED': { text: '已报价', color: 'green' }, 'REJECTED': { text: '已拒绝', color: 'red' }, 'ACCEPTED': { text: '已接受', color: 'blue' } };
          const cfg = map[s] || { text: s, color: 'default' };
          return <Tag color={cfg.color}>{cfg.text}</Tag>;
        }
      },
      { title: '提交时间', dataIndex: 'inquiryTime', width: 180 },
      {
        title: '操作', width: 200, render: (_, record) => {
          if (record.status !== 'PENDING') return null;
          return (
            <Space>
              <Button type="link" size="small" onClick={() => handleInquiryAction(record, 'quote')}>报价</Button>
              <Button type="link" danger size="small" onClick={() => handleInquiryAction(record, 'reject')}>拒绝</Button>
            </Space>
          )
        }
      }
    ];

    return (
      <Space direction="vertical" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>询价管理</Title>
          <Button onClick={loadInquiries}>刷新</Button>
        </div>
        <Table
          dataSource={filteredInquiries}
          columns={columns}
          rowKey="inquiryId"
          loading={loadingInquiries}
          pagination={{ pageSize: 10 }}
        />
        <Modal
          title={inquiryActionType === 'quote' ? '询价回复 / 报价' : '拒绝询价'}
          open={inquiryReplyModalVisible}
          onCancel={() => setInquiryReplyModalVisible(false)}
          onOk={submitInquiryReply}
        >
          <Form form={inquiryForm} layout="vertical">
            {inquiryActionType === 'quote' && (
              <Form.Item name="quotedPrice" label="报价(元)" rules={[{ required: true, message: '请输入价格' }]}>
                <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            )}
            <Form.Item name="adminReply" label="回复说明" rules={[{ required: true, message: '请输入回复内容' }]}>
              <Input.TextArea rows={4} />
            </Form.Item>
          </Form>
        </Modal>
      </Space>
    );
  };

  const menuItems: MenuProps['items'] = [
    { key: 'dashboard', label: '工作台', icon: <DashboardOutlined /> },
    { key: 'orders', label: '订单管理', icon: <ShoppingOutlined /> },
    { key: 'shipments', label: '发货管理', icon: <CarOutlined /> },
    { key: 'inquiry', label: '询价管理', icon: <QuestionCircleOutlined /> },
    { key: 'inventory', label: '库存中心', icon: <AppstoreOutlined /> },
    { key: 'purchase', label: '采购中心', icon: <ShopOutlined /> },
    { key: 'book', label: '书目库', icon: <BookOutlined /> },
    { key: 'customer', label: '客户列表', icon: <TeamOutlined /> },
    { key: 'supplier', label: '供应商库', icon: <UsergroupAddOutlined /> },
  ];

  const logout = () => {
    navigate('/login');
  };

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#3b82f6', // Fresh Blue
          borderRadius: 12, // Increased border radius
          colorBgContainer: 'rgba(255, 255, 255, 0.95)',
          fontFamily: "'Inter', sans-serif",
          colorText: '#334155', // Softer black
          colorTextSecondary: '#64748b'
        },
        components: {
          Card: {
            colorBgContainer: 'rgba(255, 255, 255, 0.9)',
            // Box shadow handled by CSS now for more complexity
          },
          Table: {
            colorBgContainer: 'transparent',
            headerBg: 'transparent',
            rowHoverBg: '#f8fafc'
          },
          Menu: {
            itemSelectedBg: '#eff6ff',
            itemSelectedColor: '#2563eb',
            itemBorderRadius: 12
          },
          Modal: {
            contentBg: '#ffffff',
            headerBg: 'transparent'
          }
        }
      }}
    >
      <>
        <Layout style={{ minHeight: '100vh' }} className="mesh-background">
          <Header
            className="glass-panel"
            style={{
              padding: '0 32px',
              background: 'rgba(255, 255, 255, 0.8)', // Slightly increased opacity
              backdropFilter: 'blur(20px)',
              display: 'flex',
              justifyContent: 'flex-end',
              alignItems: 'center',
              borderBottom: '1px solid rgba(241, 245, 249, 0.8)',
              boxShadow: '0 1px 2px rgba(0, 0, 0, 0.02)',
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              zIndex: 100,
              height: 64
            }}
          >
            <Space size="middle">
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                padding: '6px 16px',
                background: 'rgba(241, 245, 249, 0.5)',
                borderRadius: 20,
                border: '1px solid rgba(226, 232, 240, 0.5)'
              }}>
                <div style={{
                  width: 32,
                  height: 32,
                  borderRadius: '12px', // Softer square
                  background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)', // Fresh Blue
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: 600,
                  fontSize: 14,
                  boxShadow: '0 2px 6px rgba(59, 130, 246, 0.3)'
                }}>
                  {adminName.charAt(0).toUpperCase()}
                </div>
                <span style={{ fontWeight: 500, color: '#475569' }}>{adminName}</span>
              </div>
              <Button
                type="text"
                onClick={() => logout()}
                style={{ color: '#94a3b8' }}
              >
                退出登录
              </Button>
            </Space>
          </Header>
          <Layout style={{ background: 'transparent', marginTop: 64 }} hasSider>
            <Sider
              width={260}
              theme="light"
              className="glass-panel"
              style={{
                background: 'rgba(255, 255, 255, 0.6)',
                backdropFilter: 'blur(20px)',
                borderRight: '1px solid rgba(255,255,255,0.3)',
                overflow: 'auto',
                height: 'calc(100vh - 64px)',
                position: 'fixed',
                left: 0,
                top: 64,
                bottom: 0,
                zIndex: 10
              }}
            >
              <div style={{
                padding: '24px 20px 12px 20px',
                marginBottom: 8
              }}>
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12
                }}>
                  <div style={{
                    width: 36,
                    height: 36,
                    borderRadius: 10,
                    background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)', // Blue to Cyan gradient
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 10px rgba(59, 130, 246, 0.25)'
                  }}>
                    <BookOutlined style={{ color: 'white', fontSize: 18 }} />
                  </div>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 16, color: '#334155', letterSpacing: '-0.02em' }}>BookStore</div>
                    <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 500 }}>Admin Portal</div>
                  </div>
                </div>
              </div>
              <Menu
                mode="inline"
                selectedKeys={[selectedKey]}
                style={{ background: 'transparent', borderRight: 0 }}
                items={menuItems}
                onClick={(info) => setSelectedKey(info.key as MenuKey)}
              />
            </Sider>
            <Content style={{
              margin: '24px 16px',
              marginLeft: 276,
              overflow: 'initial',
              minHeight: 280
            }}>
              <div style={{
                padding: 28,
                background: 'rgba(255, 255, 255, 0.85)',
                backdropFilter: 'blur(24px)',
                borderRadius: 16,
                boxShadow: '0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04)',
                border: '1px solid rgba(255, 255, 255, 0.6)'
              }}>
                <div className="animate-view-transition" key={selectedKey}>
                  {renderContent()}
                </div>
              </div>
            </Content>
          </Layout>
        </Layout>

        {/* Modals are siblings now */}
        <Modal
          open={shipModalVisible}
          title={currentShipOrder ? `整单发货 - 订单 ${currentShipOrder.orderId}` : '整单发货'}
          onCancel={() => setShipModalVisible(false)}
          onOk={submitShip}
          okText="确认发货"
          cancelText="取消"
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Text>快递公司：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={shipCarrier}
                onChange={(e) => setShipCarrier(e.target.value)}
              />
            </div>
            <div>
              <Text>运单号：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={shipTracking}
                onChange={(e) => setShipTracking(e.target.value)}
              />
            </div>
          </Space>
        </Modal>

        {/* 分次发货弹窗 */}
        <Modal
          open={partialModalVisible}
          title={currentShipOrder ? `分次发货 - 订单 ${currentShipOrder.orderId}` : '分次发货'}
          onCancel={() => setPartialModalVisible(false)}
          onOk={submitPartial}
          okText="确认发货"
          cancelText="取消"
          width={860}
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>快递公司：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={shipCarrier}
                  onChange={(e) => setShipCarrier(e.target.value)}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>运单号：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={shipTracking}
                  onChange={(e) => setShipTracking(e.target.value)}
                />
              </div>
            </Space>
            <Table<SalesOrderItemDto>
              rowKey="orderItemId"
              size="small"
              pagination={false}
              dataSource={
                partialDetail?.items.filter((it) => {
                  const shipped = it.shippedQuantity ?? 0;
                  return it.quantity - shipped > 0;
                }) || []
              }
              columns={[
                { title: '书号', dataIndex: 'bookId' },
                { title: '订购数量', dataIndex: 'quantity' },
                {
                  title: '已发货',
                  dataIndex: 'shippedQuantity',
                  render: (v: number | null) => v ?? 0
                },
                {
                  title: '本次发货数量',
                  key: 'shipQuantity',
                  render: (_: any, r) => {
                    const shipped = r.shippedQuantity ?? 0;
                    const remain = r.quantity - shipped;
                    return (
                      <input
                        type="number"
                        min={0}
                        max={remain}
                        value={partialItems[r.orderItemId] ?? remain}
                        onChange={(e) => {
                          const val = Number(e.target.value);
                          setPartialItems((prev) => ({
                            ...prev,
                            [r.orderItemId]: Number.isFinite(val) ? val : 0
                          }));
                        }}
                        style={{ width: 120, padding: 4 }}
                      />
                    );
                  }
                }
              ]}
            />
          </Space>
        </Modal>

        <Modal
          open={orderDetailVisible}
          title={activeOrderDetail ? `订单详情 - ${activeOrderDetail.order.orderId}` : '订单详情'}
          footer={null}
          width={860}
          onCancel={() => {
            setOrderDetailVisible(false);
            setActiveOrderDetail(null);
          }}
        >
          {activeOrderDetail && (
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              <Descriptions bordered size="small" column={2}>
                <Descriptions.Item label="订单号">
                  {activeOrderDetail.order.orderId}
                </Descriptions.Item>
                <Descriptions.Item label="客户ID">
                  {activeOrderDetail.order.customerId}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  {orderStatusMap[activeOrderDetail.order.orderStatus] || activeOrderDetail.order.orderStatus}
                </Descriptions.Item>
                <Descriptions.Item label="下单时间">
                  {activeOrderDetail.order.orderTime}
                </Descriptions.Item>
                <Descriptions.Item label="应付金额" span={2}>
                  ¥{activeOrderDetail.order.payableAmount.toFixed(2)}
                </Descriptions.Item>
                <Descriptions.Item label="收货地址快照" span={2}>
                  {activeOrderDetail.order.shippingAddressSnapshot || '-'}
                </Descriptions.Item>
              </Descriptions>

              <div>
                <Title level={5}>订单明细</Title>
                <Table<SalesOrderItemDto>
                  rowKey="orderItemId"
                  size="small"
                  pagination={false}
                  dataSource={activeOrderDetail.items}
                  columns={[
                    { title: '书号', dataIndex: 'bookId' },
                    { title: '数量', dataIndex: 'quantity' },
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
                    { title: '已发货', dataIndex: 'shippedQuantity', render: (v: number | null) => v ?? 0 },
                    { title: '已收货', dataIndex: 'receivedQuantity', render: (v: number | null) => v ?? 0 },
                    { title: '明细状态', dataIndex: 'itemStatus', render: (v: string) => <Tag>{itemStatusMap[v] || v}</Tag> }
                  ]}
                />
              </div>

              <div>
                <Title level={5}>发货记录</Title>
                <Table<ShipmentDto>
                  rowKey="shipmentId"
                  size="small"
                  pagination={false}
                  dataSource={activeOrderDetail.shipments}
                  columns={[
                    { title: '发货单号', dataIndex: 'shipmentId' },
                    { title: '快递公司', dataIndex: 'carrier' },
                    { title: '快递单号', dataIndex: 'trackingNumber' },
                    { title: '发货时间', dataIndex: 'shipTime' },
                    { title: '状态', dataIndex: 'shipmentStatus', render: (v: string) => <Tag>{shipmentStatusMap[v] || v}</Tag> },
                    { title: '操作员', dataIndex: 'operator' }
                  ]}
                />
              </div>
            </Space>
          )}
        </Modal>

        {/* 添加/编辑供应商弹窗 */}
        <Modal
          open={showAddSupplier}
          title={editingSupplier ? '编辑供应商' : '添加供应商'}
          onCancel={() => {
            setShowAddSupplier(false);
            setEditingSupplier(null);
            setNewSupplier({
              supplierName: '',
              contactPerson: '',
              phone: '',
              email: '',
              address: '',
              paymentTerms: ''
            });
          }}
          onOk={submitAddSupplier}
          okText="保存"
          cancelText="取消"
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Text>名称：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.supplierName}
                onChange={(e) => setNewSupplier({ ...newSupplier, supplierName: e.target.value })}
              />
            </div>
            <div>
              <Text>联系人：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.contactPerson}
                onChange={(e) => setNewSupplier({ ...newSupplier, contactPerson: e.target.value })}
              />
            </div>
            <div>
              <Text>电话：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.phone}
                onChange={(e) => setNewSupplier({ ...newSupplier, phone: e.target.value })}
              />
            </div>
            <div>
              <Text>邮箱：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.email}
                onChange={(e) => setNewSupplier({ ...newSupplier, email: e.target.value })}
              />
            </div>
            <div>
              <Text>地址：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.address}
                onChange={(e) => setNewSupplier({ ...newSupplier, address: e.target.value })}
              />
            </div>
            <div>
              <Text>结算条款（可选）：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newSupplier.paymentTerms}
                onChange={(e) => setNewSupplier({ ...newSupplier, paymentTerms: e.target.value })}
              />
            </div>
          </Space>
        </Modal>

        {/* 供应商供货清单弹窗 */}
        <Modal
          open={supplyListVisible}
          title={activeSupplier ? `供货清单 - ${activeSupplier.supplierName}` : '供货清单'}
          onCancel={() => {
            setSupplyListVisible(false);
            setActiveSupplier(null);
            setSupplyList([]);
            setEditingSupply(null);
          }}
          footer={null}
          width="90%"
          style={{ maxWidth: 1400 }}
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Button type="primary" onClick={() => setShowAddSupply(true)}>
              添加供货关系
            </Button>
            <div style={{ overflowX: 'auto' }}>
              <Table<any>
                rowKey={(r) => `${r.supplierId}-${r.bookId}`}
                size="small"
                loading={loadingSupplyList}
                dataSource={supplyList}
                pagination={{ pageSize: 10 }}
                scroll={{ x: 'max-content' }}
                columns={[
                { title: '书号', dataIndex: 'bookId', width: 120 },
                {
                  title: '类型',
                  dataIndex: 'bookId',
                  width: 70,
                  render: (_: string, record: any) => {
                    // 优先使用后端返回的书籍类型信息
                    if (record.bookSeriesFlag) {
                      return <Tag color="purple">丛书</Tag>;
                    }
                    if (record.bookParentBookId) {
                      return <Tag color="blue">子书</Tag>;
                    }
                    // 如果后端没有返回，尝试从books状态中查找
                    const book = books.find(b => b.bookId === record.bookId);
                    if (book?.seriesFlag) return <Tag color="purple">丛书</Tag>;
                    if (book?.parentBookId) return <Tag color="blue">子书</Tag>;
                    return <Tag>普通</Tag>;
                  }
                },
                { title: '书名', dataIndex: 'bookTitle', width: 200 },
                { title: 'ISBN', dataIndex: 'bookIsbn', width: 140 },
                { title: '出版社', dataIndex: 'bookPublisher', width: 150 },
                {
                  title: '书目定价',
                  dataIndex: 'bookPrice',
                  width: 100,
                  render: (v: number | undefined) => (v != null ? `¥${v.toFixed(2)}` : '-')
                },
                {
                  title: '供货价',
                  dataIndex: 'supplyPrice',
                  width: 120,
                  render: (v: number | undefined, record: any) => {
                    if (editingSupply && editingSupply.bookId === record.bookId) {
                      return (
                        <InputNumber
                          min={0}
                          precision={2}
                          style={{ width: '100%' }}
                          value={editingSupply.supplyPrice}
                          onChange={(val) =>
                            setEditingSupply({ ...editingSupply, supplyPrice: Number(val || 0) })
                          }
                        />
                      );
                    }
                    return v != null ? `¥${v.toFixed(2)}` : '-';
                  }
                },
                {
                  title: '提前期(天)',
                  dataIndex: 'leadTimeDays',
                  width: 120,
                  render: (v: number | null | undefined, record: any) => {
                    if (editingSupply && editingSupply.bookId === record.bookId) {
                      return (
                        <InputNumber
                          min={0}
                          style={{ width: '100%' }}
                          value={editingSupply.leadTimeDays}
                          onChange={(val) =>
                            setEditingSupply({ ...editingSupply, leadTimeDays: Number(val || 0) })
                          }
                        />
                      );
                    }
                    return v != null ? v : '-';
                  }
                },
                {
                  title: '是否主供货商',
                  dataIndex: 'primary',
                  width: 130,
                  render: (v: boolean, record: any) => {
                    if (editingSupply && editingSupply.bookId === record.bookId) {
                      return (
                        <Checkbox
                          checked={editingSupply.primary}
                          onChange={(e) =>
                            setEditingSupply({ ...editingSupply, primary: e.target.checked })
                          }
                        >
                          主供货商
                        </Checkbox>
                      );
                    }
                    return v ? <Tag color="green">是</Tag> : <Tag>否</Tag>;
                  }
                },
                {
                  title: '操作',
                  key: 'action',
                  width: 200,
                  render: (_: any, record: any) => {
                    if (editingSupply && editingSupply.bookId === record.bookId) {
                      return (
                        <Space>
                          <Button size="small" type="primary" onClick={() => saveSupplyEdit(editingSupply)}>
                            保存
                          </Button>
                          <Button size="small" onClick={() => setEditingSupply(null)}>
                            取消
                          </Button>
                        </Space>
                      );
                    }
                    return (
                      <Space>
                        <Button size="small" onClick={() => setEditingSupply({ ...record })}>
                          编辑
                        </Button>
                        <Popconfirm
                          title="确定要删除这条供货关系吗？"
                          onConfirm={() => deleteSupply(record)}
                          okText="确定"
                          cancelText="取消"
                        >
                          <Button size="small" danger>
                            删除
                          </Button>
                        </Popconfirm>
                      </Space>
                    );
                  }
                }
              ]}
              />
            </div>
          </Space>
        </Modal>

        {/* 添加供货关系弹窗（供应商视角） */}
        <Modal
          open={showAddSupply}
          title="添加供货关系"
          onCancel={() => {
            setShowAddSupply(false);
            setNewSupplyForSupplier({
              bookId: '',
              supplyPrice: 0,
              leadTimeDays: undefined,
              primary: false
            });
          }}
          onOk={submitAddSupplyForSupplier}
          okText="保存"
          cancelText="取消"
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Text>选择书籍：</Text>
              <Select
                style={{ width: '100%' }}
                showSearch
                placeholder="输入书号或书名搜索"
                value={newSupplyForSupplier.bookId || undefined}
                onChange={(val) =>
                  setNewSupplyForSupplier({ ...newSupplyForSupplier, bookId: val })
                }
                filterOption={(input, option) =>
                  (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
                }
                options={books.map(b => ({
                  label: `${b.seriesFlag ? '📚 ' : b.parentBookId ? '  └ ' : ''}${b.bookId} - ${b.title}`,
                  value: b.bookId
                }))}
              />
            </div>
            <div>
              <Text>供货价（可选）：</Text>
              <InputNumber
                style={{ width: '100%' }}
                min={0}
                precision={2}
                value={newSupplyForSupplier.supplyPrice}
                onChange={(val) =>
                  setNewSupplyForSupplier({ ...newSupplyForSupplier, supplyPrice: Number(val || 0) })
                }
              />
            </div>
            <div>
              <Text>提前期天数（可选）：</Text>
              <InputNumber
                style={{ width: '100%' }}
                min={0}
                value={newSupplyForSupplier.leadTimeDays}
                onChange={(val) =>
                  setNewSupplyForSupplier({ ...newSupplyForSupplier, leadTimeDays: Number(val || 0) })
                }
              />
            </div>
            <div>
              <Checkbox
                checked={newSupplyForSupplier.primary}
                onChange={(e) =>
                  setNewSupplyForSupplier({ ...newSupplyForSupplier, primary: e.target.checked })
                }
              >
                设为主供货商
              </Checkbox>
            </div>
          </Space>
        </Modal>

        {/* 创建采购单弹窗 */}
        <Modal
          open={createPoVisible}
          title="根据选中缺书记录生成采购单"
          onCancel={() => setCreatePoVisible(false)}
          onOk={submitCreatePo}
          okText="创建采购单"
          cancelText="取消"
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Text>供应商ID：</Text>
              <InputNumber
                style={{ width: '100%' }}
                min={1}
                value={poSupplierId as number | null}
                onChange={(val) => setPoSupplierId(val as number | null)}
              />
            </div>
            <div>
              <Text>期望到货日期（YYYY-MM-DD，可选）：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                placeholder="例如 2025-12-31"
                value={poExpectedDate}
                onChange={(e) => setPoExpectedDate(e.target.value)}
              />
            </div>
            <div>
              <Text>采购员：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={poBuyer}
                onChange={(e) => setPoBuyer(e.target.value)}
              />
            </div>
          </Space>
        </Modal>

        {/* 采购单详情弹窗 */}
        <Modal
          open={poModalVisible}
          title={activePo ? `采购单详情 - ${activePo.purchaseOrderId}` : '采购单详情'}
          footer={null}
          width={820}
          onCancel={() => {
            setPoModalVisible(false);
            setActivePo(null);
            setActivePoItems([]);
          }}
        >
          {activePo && (
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              <Descriptions bordered size="small" column={2}>
                <Descriptions.Item label="采购单号">
                  {activePo.purchaseOrderId}
                </Descriptions.Item>
                <Descriptions.Item label="供应商ID">
                  {activePo.supplierId}
                </Descriptions.Item>
                <Descriptions.Item label="创建日期">
                  {activePo.createDate || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="期望到货日期">
                  {activePo.expectedDate || '-'}
                </Descriptions.Item>
                <Descriptions.Item label="采购员">
                  {activePo.buyer}
                </Descriptions.Item>
                <Descriptions.Item label="预估金额">
                  {activePo.estimatedAmount != null ? `¥${activePo.estimatedAmount.toFixed(2)}` : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="状态" span={2}>
                  {activePo.status}
                </Descriptions.Item>
              </Descriptions>

              <div>
                <Title level={5}>采购明细</Title>
                <Table<any>
                  rowKey="bookId"
                  size="small"
                  pagination={false}
                  dataSource={activePoItems}
                  columns={[
                    { title: '书号', dataIndex: 'bookId' },
                    { title: '采购数量', dataIndex: 'purchaseQuantity' },
                    {
                      title: '采购单价',
                      dataIndex: 'purchasePrice',
                      render: (v: number) => (v != null ? `¥${v.toFixed(2)}` : '-')
                    },
                    { title: '关联缺书记录ID', dataIndex: 'relatedOutOfStockId' }
                  ]}
                />
              </div>
            </Space>
          )}
        </Modal>

        {/* 添加书目弹窗 */}
        <Modal
          open={showAddBook}
          title="添加书目"
          onCancel={() => setShowAddBook(false)}
          onOk={submitAddBook}
          okText="保存"
          cancelText="取消"
          width={720}
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>书号：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={newBook.bookId}
                  onChange={(e) => setNewBook({ ...newBook, bookId: e.target.value })}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>ISBN：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={newBook.isbn}
                  onChange={(e) => setNewBook({ ...newBook, isbn: e.target.value })}
                />
              </div>
            </Space>
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>书名：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={newBook.title}
                  onChange={(e) => setNewBook({ ...newBook, title: e.target.value })}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>出版社：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={newBook.publisher}
                  onChange={(e) => setNewBook({ ...newBook, publisher: e.target.value })}
                />
              </div>
            </Space>
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>定价：</Text>
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  precision={2}
                  value={newBook.price}
                  onChange={(val) => setNewBook({ ...newBook, price: Number(val || 0) })}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>封面URL（可选）：</Text>
                <input
                  style={{ width: '100%', padding: 8 }}
                  value={newBook.coverImageUrl}
                  onChange={(e) => setNewBook({ ...newBook, coverImageUrl: e.target.value })}
                />
              </div>
            </Space>
            <div>
              <Text>目录（可选）：</Text>
              <textarea
                style={{ width: '100%', padding: 8, minHeight: 80 }}
                value={newBook.catalog}
                onChange={(e) => setNewBook({ ...newBook, catalog: e.target.value })}
              />
            </div>
            {/* 丛书类型选择 */}
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>书籍类型：</Text>
                <Select
                  style={{ width: '100%' }}
                  value={
                    newBook.seriesFlag ? 'series' :
                      (newBook.parentBookId !== '' ? 'child' : 'normal')
                  }
                  onChange={(val) => {
                    if (val === 'series') {
                      setNewBook({ ...newBook, seriesFlag: true, parentBookId: '' });
                    } else if (val === 'child') {
                      // 子书：清空parentBookId让用户选择，但标记为子书模式
                      setNewBook({ ...newBook, seriesFlag: false, parentBookId: '_pending_' });
                    } else {
                      setNewBook({ ...newBook, seriesFlag: false, parentBookId: '' });
                    }
                  }}
                  options={[
                    { label: '普通书籍', value: 'normal' },
                    { label: '📚 丛书（套装）', value: 'series' },
                    { label: '子书（属于某丛书）', value: 'child' }
                  ]}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>所属丛书（如是子书）：</Text>
                <Select
                  style={{ width: '100%' }}
                  value={newBook.parentBookId && newBook.parentBookId !== '_pending_' ? newBook.parentBookId : undefined}
                  onChange={(val) => setNewBook({ ...newBook, parentBookId: val || '_pending_' })}
                  placeholder="选择父丛书"
                  allowClear
                  disabled={newBook.seriesFlag || newBook.parentBookId === ''}
                  showSearch
                  filterOption={(input, option) =>
                    (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
                  }
                  options={books.filter(b => b.seriesFlag).map(b => ({ label: `${b.bookId} - ${b.title}`, value: b.bookId }))}
                />
              </div>
            </Space>
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>初始库存数量：</Text>
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  value={newBook.initQuantity}
                  onChange={(val) => setNewBook({ ...newBook, initQuantity: Number(val || 0) })}
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>安全库存：</Text>
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  value={newBook.safetyStock}
                  onChange={(val) => setNewBook({ ...newBook, safetyStock: Number(val || 0) })}
                />
              </div>
            </Space>
          </Space>
        </Modal>

        {/* 添加缺书记录弹窗 */}
        <Modal
          open={showAddOos}
          title="添加缺书记录"
          onCancel={() => setShowAddOos(false)}
          onOk={submitAddOos}
          okText="保存"
          cancelText="取消"
        >
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <div>
              <Text>书号：</Text>
              <input
                style={{ width: '100%', padding: 8 }}
                value={newOos.bookId}
                onChange={(e) => setNewOos({ ...newOos, bookId: e.target.value })}
              />
            </div>
            <Space style={{ width: '100%' }}>
              <div style={{ flex: 1 }}>
                <Text>需求数量：</Text>
                <InputNumber
                  style={{ width: '100%' }}
                  min={1}
                  value={newOos.requiredQuantity}
                  onChange={(val) =>
                    setNewOos({
                      ...newOos,
                      requiredQuantity: Number(val || 1)
                    })
                  }
                />
              </div>
              <div style={{ flex: 1 }}>
                <Text>优先级（可选）：</Text>
                <InputNumber
                  style={{ width: '100%' }}
                  min={1}
                  value={newOos.priority}
                  onChange={(val) =>
                    setNewOos({
                      ...newOos,
                      priority: Number(val || 1)
                    })
                  }
                />
              </div>
            </Space>
            <Text type="secondary">
              说明：与桌面端一致，此处仅登记书号与需求数量，来源标记为 MANUAL，状态为 PENDING，统一进入缺书记录表。
            </Text>
          </Space>
        </Modal>

        {/* 书目详情 / 作者关键字 / 供货关系维护弹窗 */}
        <Modal
          open={bookDetailVisible}
          title={activeBook ? `书目详情 - ${activeBook.bookId}` : '书目详情'}
          onCancel={() => {
            setBookDetailVisible(false);
            setActiveBook(null);
            setEditBook(null);
            setAuthors([]);
            setKeywords([]);
            setSupplies([]);
          }}
          footer={null}
          width={960}
        >
          {loadingMeta || !editBook ? (
            <Text>加载中...</Text>
          ) : (
            <Space direction="vertical" style={{ width: '100%' }} size="large">
              {/* 基本信息编辑 */}
              <Space direction="vertical" style={{ width: '100%' }} size="small">
                <Title level={5}>基本信息</Title>
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>书名：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={editBook.title}
                      onChange={(e) => setEditBook({ ...editBook, title: e.target.value })}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>ISBN：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={editBook.isbn}
                      onChange={(e) => setEditBook({ ...editBook, isbn: e.target.value })}
                    />
                  </div>
                </Space>
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>出版社：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={editBook.publisher}
                      onChange={(e) => setEditBook({ ...editBook, publisher: e.target.value })}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>版次：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={editBook.edition}
                      onChange={(e) => setEditBook({ ...editBook, edition: e.target.value })}
                    />
                  </div>
                </Space>
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>定价：</Text>
                    <InputNumber
                      style={{ width: '100%' }}
                      min={0}
                      precision={2}
                      value={editBook.price}
                      onChange={(val) => setEditBook({ ...editBook, price: Number(val || 0) })}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>状态：</Text>
                    <Select
                      style={{ width: '100%' }}
                      value={editBook.status}
                      onChange={(val) => setEditBook({ ...editBook, status: val })}
                      options={[
                        { label: '在售 AVAILABLE', value: 'AVAILABLE' },
                        { label: '下架 UNAVAILABLE', value: 'UNAVAILABLE' }
                      ]}
                    />
                  </div>
                </Space>
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>封面URL：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={editBook.coverImageUrl}
                      onChange={(e) => setEditBook({ ...editBook, coverImageUrl: e.target.value })}
                    />
                  </div>
                </Space>
                <div>
                  <Text>目录：</Text>
                  <textarea
                    style={{ width: '100%', padding: 8, minHeight: 80 }}
                    value={editBook.catalog}
                    onChange={(e) => setEditBook({ ...editBook, catalog: e.target.value })}
                  />
                </div>
                <Button type="primary" loading={bookDetailSaving} onClick={submitBookBaseInfo}>
                  保存基本信息
                </Button>
              </Space>

              {/* 作者维护 */}
              <Space direction="vertical" style={{ width: '100%' }} size="small">
                <Title level={5}>作者</Title>
                <Table<AuthorDto>
                  rowKey="authorId"
                  size="small"
                  pagination={false}
                  dataSource={authors}
                  columns={[
                    { title: '作者ID', dataIndex: 'authorId', width: 100 },
                    { title: '姓名', dataIndex: 'authorName' },
                    { title: '国籍', dataIndex: 'nationality' },
                    {
                      title: '作者顺序',
                      dataIndex: 'authorOrder',
                      width: 140,
                      render: (v: number | undefined, row) => (
                        <InputNumber
                          min={1}
                          value={v ?? 1}
                          onChange={(val) => updateAuthorOrder(row, Number(val || 1))}
                        />
                      )
                    },
                    {
                      title: '操作',
                      key: 'action',
                      width: 120,
                      render: (_, row) => (
                        <Button danger size="small" onClick={() => removeAuthor(row)}>
                          移除
                        </Button>
                      )
                    }
                  ]}
                />
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>作者姓名：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={newAuthor.authorName}
                      onChange={(e) => setNewAuthor({ ...newAuthor, authorName: e.target.value })}
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>国籍（可选）：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={newAuthor.nationality}
                      onChange={(e) => setNewAuthor({ ...newAuthor, nationality: e.target.value })}
                    />
                  </div>
                  <div>
                    <Text>顺序：</Text>
                    <InputNumber
                      min={1}
                      style={{ width: 80, marginLeft: 8 }}
                      value={newAuthor.authorOrder}
                      onChange={(val) => setNewAuthor({ ...newAuthor, authorOrder: Number(val || 1) })}
                    />
                  </div>
                  <Button type="dashed" onClick={addAuthor}>
                    添加作者
                  </Button>
                </Space>
              </Space>

              {/* 关键字维护 */}
              <Space direction="vertical" style={{ width: '100%' }} size="small">
                <Title level={5}>关键字</Title>
                <Table<KeywordDto>
                  rowKey="keywordId"
                  size="small"
                  pagination={false}
                  dataSource={keywords}
                  columns={[
                    { title: '关键字ID', dataIndex: 'keywordId', width: 100 },
                    { title: '关键字', dataIndex: 'keywordText' },
                    {
                      title: '操作',
                      key: 'action',
                      width: 120,
                      render: (_, row) => (
                        <Button danger size="small" onClick={() => removeKeyword(row)}>
                          移除
                        </Button>
                      )
                    }
                  ]}
                />
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>新增关键字：</Text>
                    <input
                      style={{ width: '100%', padding: 8 }}
                      value={newKeyword.keywordText}
                      onChange={(e) => setNewKeyword({ keywordText: e.target.value })}
                    />
                  </div>
                  <Button type="dashed" onClick={addKeyword}>
                    添加关键字
                  </Button>
                </Space>
              </Space>

              {/* 供货关系维护 */}
              <Space direction="vertical" style={{ width: '100%' }} size="small">
                <Title level={5}>供货关系</Title>
                <Table<SupplyDto>
                  rowKey={(r) => `${r.supplierId}-${r.bookId}`}
                  size="small"
                  pagination={false}
                  dataSource={supplies}
                  columns={[
                    { title: '供应商ID', dataIndex: 'supplierId', width: 100 },
                    {
                      title: '供货价',
                      dataIndex: 'supplyPrice',
                      render: (v: number | undefined) => (v != null ? `¥${v.toFixed(2)}` : '-')
                    },
                    { title: '提前期(天)', dataIndex: 'leadTimeDays', width: 120 },
                    {
                      title: '是否主供货商',
                      dataIndex: 'primary',
                      width: 120,
                      render: (v: boolean) => (v ? <Tag color="green">是</Tag> : <Tag>否</Tag>)
                    },
                    {
                      title: '操作',
                      key: 'action',
                      width: 120,
                      render: (_, row) => (
                        <Button danger size="small" onClick={() => removeSupply(row)}>
                          删除
                        </Button>
                      )
                    }
                  ]}
                />
                <Space style={{ width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <Text>供应商ID：</Text>
                    <InputNumber
                      style={{ width: '100%' }}
                      min={1}
                      value={newSupply.supplierId}
                      onChange={(val) =>
                        setNewSupply({ ...newSupply, supplierId: (val as number | null) || undefined })
                      }
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>供货价（可选）：</Text>
                    <InputNumber
                      style={{ width: '100%' }}
                      min={0}
                      precision={2}
                      value={newSupply.supplyPrice}
                      onChange={(val) =>
                        setNewSupply({
                          ...newSupply,
                          supplyPrice: typeof val === 'number' ? val : undefined
                        })
                      }
                    />
                  </div>
                  <div style={{ flex: 1 }}>
                    <Text>提前期天数（可选）：</Text>
                    <InputNumber
                      style={{ width: '100%' }}
                      min={0}
                      value={newSupply.leadTimeDays}
                      onChange={(val) =>
                        setNewSupply({
                          ...newSupply,
                          leadTimeDays: typeof val === 'number' ? val : undefined
                        })
                      }
                    />
                  </div>
                  <div>
                    <label style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <input
                        type="checkbox"
                        checked={newSupply.primary}
                        onChange={(e) =>
                          setNewSupply({
                            ...newSupply,
                            primary: e.target.checked
                          })
                        }
                      />
                      <span>设为主供货商</span>
                    </label>
                  </div>
                  <Button type="dashed" onClick={addSupply}>
                    添加供货关系
                  </Button>
                </Space>
              </Space>
            </Space>
          )}
        </Modal>
      </>
    </ConfigProvider>
  );
};

export default AdminLayout;

