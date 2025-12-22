import React, { useEffect, useState } from 'react';
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
  Checkbox
} from 'antd';
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

type MenuKey = 'orders' | 'shipments' | 'inventory' | 'purchase' | 'customer' | 'supplier' | 'book';

const AdminLayout: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state || {}) as Partial<LocationState>;
  const adminName = state.adminName || '管理员';

  const [selectedKey, setSelectedKey] = useState<MenuKey>('orders');
  const [orders, setOrders] = useState<SalesOrderDto[]>([]);
  const [loadingOrders, setLoadingOrders] = useState(false);
  const [orderStatusFilter, setOrderStatusFilter] = useState<string>('全部');
  const [activeOrderDetail, setActiveOrderDetail] = useState<OrderDetailResp | null>(null);
  const [orderDetailVisible, setOrderDetailVisible] = useState(false);
  const [shipmentOrders, setShipmentOrders] = useState<SalesOrderDto[]>([]);
  const [loadingShipOrders, setLoadingShipOrders] = useState(false);
  const [shipmentStatusFilter, setShipmentStatusFilter] = useState<string>('PENDING_SHIPMENT');
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
    safetyStock: 10
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

  useEffect(() => {
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
    }
    if (selectedKey === 'book') {
      loadBooks();
    }
  }, [selectedKey, orderStatusFilter, shipmentStatusFilter]);

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

  const openShipModal = (order: SalesOrderDto) => {
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
      await http.post('/admin/suppliers', newSupplier);
      message.success('供应商已添加');
      setShowAddSupplier(false);
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
      message.error(e?.response?.data?.message || '添加供应商失败');
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
        safetyStock: newBook.safetyStock
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
        safetyStock: 10
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

  const menuItems: MenuProps['items'] = [
    { key: 'orders', label: '订单管理' },
    { key: 'shipments', label: '发货管理' },
    { key: 'inventory', label: '库存管理' },
    { key: 'purchase', label: '采购管理' },
    { key: 'customer', label: '客户管理' },
    { key: 'supplier', label: '供应商管理' },
    { key: 'book', label: '书目管理' }
  ];

  const renderContent = () => {
    if (selectedKey === 'orders') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>订单管理</Title>
          <Space align="center">
            <span>订单状态：</span>
            <Select
              style={{ width: 260 }}
              value={orderStatusFilter}
              onChange={(val) => setOrderStatusFilter(val)}
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
            pagination={{ pageSize: 12 }}
          />
        </Space>
      );
    }
    if (selectedKey === 'shipments') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>发货管理</Title>
          <Space align="center">
            <span>订单状态：</span>
            <Select
              style={{ width: 260 }}
              value={shipmentStatusFilter}
              onChange={(val) => setShipmentStatusFilter(val)}
              options={[
                { label: '待发货 PENDING_SHIPMENT', value: 'PENDING_SHIPMENT' },
                { label: '配送中 DELIVERING', value: 'DELIVERING' },
                { label: '待付款（允许赊销） PENDING_PAYMENT', value: 'PENDING_PAYMENT' },
                { label: '全部', value: '全部' }
              ]}
            />
          </Space>
          <Table<SalesOrderDto>
            rowKey="orderId"
            size="small"
            columns={[
              ...orderColumns,
              {
                title: '操作',
                key: 'action',
                width: 200,
                render: (_, record) => {
                  const canShip =
                    record.orderStatus === 'PENDING_SHIPMENT' ||
                    record.orderStatus === 'DELIVERING' ||
                    record.orderStatus === 'PENDING_PAYMENT'; // 赊销场景
                  return (
                    <Space>
                      <a onClick={() => openOrderDetail(record.orderId)}>详情</a>
                      <a onClick={() => openShipModal(record)} style={{ opacity: canShip ? 1 : 0.4, pointerEvents: canShip ? 'auto' : 'none' }}>
                        整单发货
                      </a>
                      <a onClick={() => openPartialModal(record)} style={{ opacity: canShip ? 1 : 0.4, pointerEvents: canShip ? 'auto' : 'none' }}>
                        分次发货
                      </a>
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
          />
        </Space>
      );
    }
    if (selectedKey === 'customer') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>客户管理</Title>
          <Table<CustomerDto>
            rowKey="customerId"
            size="small"
            loading={loadingCustomers}
            dataSource={customers}
            pagination={{ pageSize: 12 }}
            columns={[
              { title: '客户ID', dataIndex: 'customerId', width: 90 },
              { title: '用户名', dataIndex: 'username' },
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
              { title: '信用等级ID', dataIndex: 'creditLevelId', width: 120 },
              {
                title: '操作',
                key: 'action',
                width: 200,
                render: (_, r) => (
                  <Space>
                    {[1, 2, 3, 4, 5].map((lv) => (
                      <Button
                        key={lv}
                        size="small"
                        type={r.creditLevelId === lv ? 'primary' : 'default'}
                        onClick={() => updateCustomerCredit(r, lv)}
                      >
                        {lv}级
                      </Button>
                    ))}
                  </Space>
                )
              }
            ]}
          />
        </Space>
      );
    }
    if (selectedKey === 'supplier') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>供应商管理</Title>
          <Button type="primary" onClick={() => setShowAddSupplier(true)}>
            添加供应商
          </Button>
          <Table<any>
            rowKey="supplierId"
            size="small"
            loading={loadingSuppliers}
            dataSource={suppliers}
            pagination={{ pageSize: 12 }}
            columns={[
              { title: '供应商ID', dataIndex: 'supplierId', width: 90 },
              { title: '名称', dataIndex: 'supplierName' },
              { title: '联系人', dataIndex: 'contactPerson' },
              { title: '电话', dataIndex: 'phone' },
              { title: '邮箱', dataIndex: 'email' },
              { title: '地址', dataIndex: 'address' },
              { title: '状态', dataIndex: 'cooperationStatus', width: 100 },
              {
                title: '操作',
                key: 'action',
                width: 150,
                render: (_: any, record: any) => (
                  <Button size="small" type="link" onClick={() => openSupplyList(record)}>
                    查看供货清单
                  </Button>
                )
              }
            ]}
          />
        </Space>
      );
    }
    if (selectedKey === 'book') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>书目管理</Title>
          <Button type="primary" onClick={() => setShowAddBook(true)}>
            添加书目
          </Button>
          <Table<BookDto>
            rowKey="bookId"
            size="small"
            loading={loadingBooks}
            dataSource={books}
            pagination={{ pageSize: 12 }}
            columns={[
              {
                title: '书号',
                dataIndex: 'bookId',
                width: 120,
                render: (v, record) => (
                  <a onClick={() => openBookDetail(record)}>{v}</a>
                )
              },
              { title: '书名', dataIndex: 'title', width: 220 },
              { title: 'ISBN', dataIndex: 'isbn', width: 140 },
              { title: '出版社', dataIndex: 'publisher' },
              {
                title: '定价',
                dataIndex: 'price',
                width: 120,
                render: (v: number) => (v != null ? `¥${v.toFixed(2)}` : '¥0.00')
              },
              { title: '状态', dataIndex: 'status', width: 100 }
            ]}
          />
        </Space>
      );
    }
    if (selectedKey === 'purchase') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>采购管理</Title>

          {/* 缺书记录 + 生成采购单 */}
          <Space direction="vertical" style={{ width: '100%' }}>
            <Space style={{ width: '100%', justifyContent: 'space-between' }}>
              <Text strong>缺书记录（PENDING）</Text>
              <Space>
                <Button onClick={() => setShowAddOos(true)}>添加缺书记录</Button>
                <Button type="primary" onClick={openCreatePoModal}>
                  根据选中缺书生成采购单
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
              pagination={{ pageSize: 10 }}
              columns={[
                { title: '记录ID', dataIndex: 'recordId', width: 100 },
                { title: '书号', dataIndex: 'bookId' },
                { title: '需求数量', dataIndex: 'requiredQuantity' },
                { title: '状态', dataIndex: 'status' }
              ]}
            />
          </Space>

          {/* 顾客缺书登记 */}
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>顾客缺书登记（待处理）</Text>
            <Table<CustomerOosRequestDto>
              rowKey="requestId"
              size="small"
              loading={loadingCustomerOos}
              dataSource={customerOos}
              pagination={{ pageSize: 10 }}
              columns={[
                { title: '登记ID', dataIndex: 'requestId', width: 90 },
                { title: '订单号', dataIndex: 'orderId', width: 90 },
                { title: '客户ID', dataIndex: 'customerId', width: 90 },
                { title: '书号', dataIndex: 'bookId' },
                { title: '订购数量', dataIndex: 'requestedQty' },
                { title: '备注', dataIndex: 'customerNote' },
                {
                  title: '操作',
                  key: 'action',
                  width: 200,
                  render: (_, r) => (
                    <Space>
                      <Button size="small" type="primary" onClick={() => handleCustomerOos(r, 'accept')}>
                        生成缺书记录
                      </Button>
                      <Button size="small" danger onClick={() => handleCustomerOos(r, 'reject')}>
                        不生成
                      </Button>
                    </Space>
                  )
                }
              ]}
            />
          </Space>

          {/* 采购单列表 */}
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text strong>采购单</Text>
            <Table<PurchaseOrderDto>
              rowKey="purchaseOrderId"
              size="small"
              loading={loadingPo}
              dataSource={purchaseOrders}
              pagination={{ pageSize: 10 }}
              columns={[
                {
                  title: '采购单号',
                  dataIndex: 'purchaseOrderId',
                  render: (v, r) => <a onClick={() => openPoDetail(r)}>{v}</a>
                },
                { title: '供应商ID', dataIndex: 'supplierId' },
                { title: '创建日期', dataIndex: 'createDate' },
                { title: '期望到货日期', dataIndex: 'expectedDate' },
                { title: '采购员', dataIndex: 'buyer' },
                {
                  title: '预估金额',
                  dataIndex: 'estimatedAmount',
                  render: (v: number) => (v != null ? `¥${v.toFixed(2)}` : '-')
                },
                { title: '状态', dataIndex: 'status' },
                {
                  title: '操作',
                  key: 'action',
                  render: (_, r) => (
                    <Space>
                      <Button size="small" onClick={() => openPoDetail(r)}>
                        明细
                      </Button>
                      <Button
                        size="small"
                        type="primary"
                        disabled={r.status === 'COMPLETED'}
                        onClick={() => receivePurchase(r)}
                      >
                        到货
                      </Button>
                    </Space>
                  )
                }
              ]}
            />
          </Space>
        </Space>
      );
    }
    if (selectedKey === 'inventory') {
      return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={4}>库存管理</Title>
          <Table<InventoryDto>
            rowKey="bookId"
            size="small"
            dataSource={inventories}
            loading={loadingInventories}
            pagination={{ pageSize: 15 }}
            columns={[
              { title: '书号', dataIndex: 'bookId' },
              { title: '库存数量', dataIndex: 'quantity' },
              {
                title: '安全库存',
                dataIndex: 'safetyStock',
                render: (v: number, record) => (
                  <InputNumber
                    min={0}
                    value={v}
                    onChange={(val) => {
                      // 先在前端更新显示，避免输入时闪烁
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
              { title: '库位', dataIndex: 'locationCode' },
              {
                title: '操作',
                key: 'action',
                width: 160,
                render: (_, record) => (
                  <Space>
                    <Button
                      type="primary"
                      size="small"
                      onClick={() => adjustInventory(record.bookId, 10)}
                    >
                      +10
                    </Button>
                    <Button
                      danger
                      size="small"
                      onClick={() => adjustInventory(record.bookId, -10)}
                    >
                      -10
                    </Button>
                  </Space>
                )
              }
            ]}
          />
        </Space>
      );
    }
    return (
      <div style={{ padding: 24 }}>
        <Title level={4}>模块开发中</Title>
        <Text type="secondary">
          该模块将按照 JavaFX AdminView 中的对应功能逐步迁移（当前已完成订单管理、发货管理、库存管理、采购管理、客户管理、供应商管理、书目管理）。
        </Text>
      </div>
    );
  };

  const logout = () => {
    navigate('/login');
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          background: '#2c3e50',
          display: 'flex',
          alignItems: 'center',
          padding: '0 24px'
        }}
      >
        <Title level={4} style={{ color: '#fff', margin: 0 }}>
          书店管理后台
        </Title>
        <div style={{ flex: 1 }} />
        <Space size="large" align="center">
          <Text style={{ color: '#fff' }}>管理员：{adminName}</Text>
          <a style={{ color: '#fff' }} onClick={logout}>
            退出
          </a>
        </Space>
      </Header>
      <Layout>
        <Sider
          width={200}
          style={{ background: '#34495e' }}
        >
          <Menu
            theme="dark"
            mode="inline"
            selectedKeys={[selectedKey]}
            items={menuItems}
            onClick={(info) => setSelectedKey(info.key as MenuKey)}
          />
        </Sider>
        <Content style={{ padding: 24, background: '#f5f5f5' }}>{renderContent()}</Content>
      </Layout>

      {/* 整单发货弹窗 */}
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
                {activeOrderDetail.order.orderStatus}
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
                  { title: '明细状态', dataIndex: 'itemStatus' }
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
                  { title: '状态', dataIndex: 'shipmentStatus' },
                  { title: '操作员', dataIndex: 'operator' }
                ]}
              />
            </div>
          </Space>
        )}
      </Modal>

      {/* 添加供应商弹窗 */}
      <Modal
        open={showAddSupplier}
        title="添加供应商"
        onCancel={() => setShowAddSupplier(false)}
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
        width={1000}
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <Button type="primary" onClick={() => setShowAddSupply(true)}>
            添加供货关系
          </Button>
          <Table<any>
            rowKey={(r) => `${r.supplierId}-${r.bookId}`}
            size="small"
            loading={loadingSupplyList}
            dataSource={supplyList}
            pagination={{ pageSize: 10 }}
            columns={[
              { title: '书号', dataIndex: 'bookId', width: 120 },
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
            <Text>书号：</Text>
            <input
              style={{ width: '100%', padding: 8 }}
              value={newSupplyForSupplier.bookId}
              onChange={(e) =>
                setNewSupplyForSupplier({ ...newSupplyForSupplier, bookId: e.target.value })
              }
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
    </Layout>
  );
};

export default AdminLayout;

