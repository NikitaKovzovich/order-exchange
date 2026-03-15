// ============================================
// API Response Types
// ============================================

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errors?: { [key: string]: string } | string[];
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first?: boolean;
  last?: boolean;
}

// ============================================
// Auth & User Types
// ============================================

export type UserRole = 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
export type LegalForm = 'IE' | 'LLC' | 'OJSC' | 'CJSC' | 'PJSC' | 'PUE';
export type CompanyStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'BLOCKED';
export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: UserRole;
  userId: number;
  companyId: number | null;
}

export interface AuthSession {
  email: string;
  role: UserRole;
  userId: number;
  companyId: number | null;
}

export interface UserProfile {
  id: number;
  email: string;
  role: UserRole;
  companyId: number | null;
  companyName: string | null;
  status: string;
  isActive: boolean;
}

export interface CompanyDocumentInfo {
  id: number;
  documentType: string;
  originalFilename: string;
  downloadUrl: string;
}

export interface CompanyProfile {
  id: number;
  name?: string | null;
  legalName: string;
  legalForm: LegalForm | string;
  legalFormText?: string | null;
  taxId: string;
  registrationDate: string;
  status: CompanyStatus | string;
  contactPhone: string;
  verified: boolean;
  addresses: CompanyAddress[];
  bankName?: string | null;
  bic?: string | null;
  accountNumber?: string | null;
  directorName?: string | null;
  chiefAccountantName?: string | null;
  documents?: CompanyDocumentInfo[];
  paymentTerms?: string | null;
}

export interface CompanyAddress {
  id: number;
  addressType: string;
  fullAddress: string;
  isDefault: boolean;
}

// ============================================
// Category Types
// ============================================

export interface Category {
  id: number;
  name: string;
  parentId: number | null;
  parentName: string | null;
  productCount: number;
}

export interface CategoryTree extends Category {
  children: CategoryTree[];
}

export interface CreateCategoryRequest {
  name: string;
  parentId?: number | null;
}

// ============================================
// Product Types
// ============================================

export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Product {
  id: number;
  supplierId: number;
  supplierName?: string;
  sku: string;
  name: string;
  description: string;
  category: Category;
  pricePerUnit: number;
  priceWithVat: number;
  unitOfMeasure: string;
  vatRateName: string;
  vatRateValue: number;
  weight: number;
  countryOfOrigin: string;
  barcode: string;
  primaryImageUrl: string | null;
  status: ProductStatus;
  quantityAvailable: number;
  inStock: boolean;
  packageDimensions?: string | null;
  productionDate?: string | null;
  expiryDate?: string | null;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description?: string;
  categoryId: number;
  pricePerUnit: number;
  unitId: number;
  vatRateId: number;
  weight?: number;
  countryOfOrigin?: string;
  barcode?: string;
  initialQuantity?: number;
}

export interface UpdateProductRequest {
  sku?: string;
  name?: string;
  description?: string;
  categoryId?: number;
  pricePerUnit?: number;
  unitId?: number;
  vatRateId?: number;
  weight?: number;
  countryOfOrigin?: string;
  barcode?: string;
}

export interface ProductSearchParams {
  categoryId?: number;
  supplierId?: number;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  status?: ProductStatus;
  page?: number;
  size?: number;
  sort?: string;
}

export interface ProductImage {
  id: number;
  productId: number;
  fileName: string;
  contentType: string;
  fileSize: number;
  primary: boolean;
  sortOrder: number;
  imageUrl: string;
}

// ============================================
// Inventory Types
// ============================================

export interface Inventory {
  productId: number;
  productName: string;
  sku: string;
  quantityAvailable: number;
  reservedQuantity: number;
  actualAvailable: number;
  lowStock: boolean;
  outOfStock: boolean;
}

export interface UpdateInventoryRequest {
  quantity: number;
  reason?: string;
}

// ============================================
// Reference Data Types
// ============================================

export interface Unit {
  id: number;
  name: string;
}

export interface VatRate {
  id: number;
  ratePercentage: number;
  description: string;
}

// ============================================
// Cart Types
// ============================================

export interface CartItem {
  id: number;
  productId: number;
  supplierId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;
  totalPrice: number;
  vatAmount: number;
  addedAt: string;
}

export interface Cart {
  id: number;
  customerId: number;
  items: CartItem[];
  itemCount: number;
  totalAmount: number;
  totalVat: number;
  supplierIds: number[];
  createdAt: string;
  updatedAt: string;
}

export interface AddToCartRequest {
  productId: number;
  supplierId: number;
  productName: string;
  productSku?: string;
  quantity: number;
  unitPrice: number;
  vatRate?: number;
}

export interface UpdateCartItemRequest {
  quantity: number;
}

export interface CheckoutRequest {
  deliveryAddress: string;
  desiredDeliveryDate: string;
  notes?: string;
}

export interface CheckoutResponse {
  orders: OrderSummary[];
  totalOrders: number;
  totalAmount: number;
  orderCount?: number;
  message?: string;
}

// ============================================
// Order Types
// ============================================

export type OrderStatus =
  | 'PENDING_CONFIRMATION'
  | 'CONFIRMED'
  | 'REJECTED'
  | 'AWAITING_PAYMENT'
  | 'PENDING_PAYMENT_VERIFICATION'
  | 'PAID'
  | 'PAYMENT_PROBLEM'
  | 'AWAITING_SHIPMENT'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'AWAITING_CORRECTION'
  | 'CLOSED'
  | 'CANCELLED';

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;
  totalPrice: number;
  vatAmount: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  supplierId: number;
  supplierName?: string;
  customerId: number;
  customerName?: string;
  statusCode: OrderStatus;
  statusName: string;
  deliveryAddress: string;
  desiredDeliveryDate: string;
  actualDeliveryDate?: string;
  totalAmount: number;
  vatAmount: number;
  items: OrderItem[];
  notes?: string;
  rejectionReason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface OrderSummary {
  id: number;
  orderNumber: string;
  supplierId: number;
  supplierName?: string;
  customerId: number;
  customerName?: string;
  statusCode: OrderStatus;
  statusName: string;
  deliveryAddress?: string;
  desiredDeliveryDate?: string;
  totalAmount: number;
  vatAmount?: number;
  itemCount: number;
  ttnGenerated?: boolean;
  createdAt: string;
}

export interface OrderHistoryEntry {
  id?: number;
  eventType?: string;
  eventName?: string;
  eventDescription?: string;
  statusFrom?: string | null;
  previousStatus?: string | null;
  previousStatusName?: string | null;
  statusTo?: string | null;
  newStatus?: string | null;
  newStatusName?: string | null;
  description?: string;
  createdBy?: number | null;
  userId?: number | null;
  createdAt?: string;
  timestamp?: string;
}

export interface OrderDocument {
  id: number;
  documentType?: string;
  documentTypeCode?: string;
  documentTypeName?: string;
  documentName?: string;
  fileName?: string;
  originalFilename?: string;
  fileKey?: string;
  url?: string;
  uploadedBy?: number;
  createdAt?: string;
  uploadedAt?: string;
}

export interface OrderStatusSummary {
  totalOrders: number;
  countByStatus: Record<string, number>;
  pendingConfirmation: number;
  awaitingShipment: number;
  inTransit: number;
  paymentProblems: number;
  awaitingDelivery: number;
  requirePayment: number;
  rejected: number;
}

export interface RejectOrderRequest {
  reason: string;
}

export interface PaymentProofRequest {
  documentKey: string;
  paymentReference: string;
  notes?: string;
}

export interface DiscrepancyItem {
  orderItemId: number;
  actualQuantity: number;
  reason: string;
}

export interface CreateDiscrepancyRequest {
  items: DiscrepancyItem[];
  notes?: string;
}

export interface DiscrepancyReport {
  id: number;
  orderId: number;
  orderNumber: string;
  items: DiscrepancyReportItem[];
  totalDiscrepancyAmount: number;
  status: string;
  notes?: string;
  createdAt: string;
  createdBy: number;
}

export interface DiscrepancyReportItem {
  orderItemId: number;
  productName: string;
  productSku: string;
  expectedQuantity: number;
  actualQuantity: number;
  discrepancy: number;
  unitPrice: number;
  discrepancyAmount: number;
  reason: string;
}

// ============================================
// Analytics Types
// ============================================

export type AnalyticsPeriod = 'week' | 'month' | 'quarter' | 'year';

export interface SupplierAnalytics {
  period: string;
  totalRevenue: number;
  totalOrders: number;
  averageOrderValue: number;
  topProducts: TopProduct[];
  topCustomers: TopCustomer[];
  ordersByStatus: { [status: string]: number };
  revenueByMonth: { [month: string]: number };
}

export interface CustomerAnalytics {
  period: string;
  totalSpent: number;
  totalOrders: number;
  averageOrderValue: number;
  topProducts: TopProduct[];
  topSuppliers: TopSupplier[];
  ordersByStatus: { [status: string]: number };
  spendingByMonth: { [month: string]: number };
}

export interface TopProduct {
  productId: number;
  productName: string;
  totalQuantity: number;
  totalRevenue: number;
}

export interface TopCustomer {
  customerId: number;
  customerName: string;
  totalOrders: number;
  totalRevenue: number;
}

export interface TopSupplier {
  supplierId: number;
  supplierName: string;
  totalOrders: number;
  totalSpent: number;
}

export interface ExpenseStructureItem {
  categoryName?: string;
  supplierId?: number;
  supplierName?: string;
  amount: number;
  percent: number;
}

export interface SupplierDashboard {
  revenueThisMonth: number;
  newOrdersToday: number;
  averageCheck: number;
  ordersInTransit: number;
  pendingConfirmationCount: number;
  pendingConfirmationOrders: Order[];
  salesDynamics7Days: Array<{
    date: string;
    revenue: number;
    orderCount: number;
  }>;
}

export interface CustomerDashboard {
  expensesThisMonth: number;
  orderCount: number;
  activeContractsCount: number;
  averageCheck: number;
  recentOrders: Order[];
  expenseStructure: {
    byCategory: ExpenseStructureItem[];
    bySupplier: ExpenseStructureItem[];
  };
  expensesDynamics7Days: Array<{
    date: string;
    expenses: number;
    orderCount: number;
  }>;
}

export interface ProductPurchaseHistoryItem {
  orderId?: number;
  orderNumber?: string;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice?: number;
  totalAmount?: number;
  orderDate?: string;
  supplierName?: string;
}

// ============================================
// Partnership Types
// ============================================

export type PartnershipStatus = 'NEW' | 'PENDING' | 'ACTIVE' | 'REJECTED';

export interface Partnership {
  id: number;
  supplierId: number;
  supplierCompanyName?: string;
  customerId: number;
  customerCompanyName: string;
  customerUnp?: string;
  status: PartnershipStatus | string;
  contractNumber?: string | null;
  contractDate?: string | null;
  contractEndDate?: string | null;
  createdAt: string;
  updatedAt?: string;
}

export interface SupplierDirectoryItem {
  companyId: number;
  companyName: string;
  taxId: string;
  contactPhone: string;
  partnershipStatus: PartnershipStatus | string;
  partnershipId?: number | null;
}

export interface CreatePartnershipRequest {
  supplierId: number;
  contractNumber: string;
  contractDate: string;
  contractEndDate: string;
  customerCompanyName: string;
  customerUnp: string;
}

export interface UpdatePartnershipContractRequest {
  contractNumber: string;
  contractDate: string;
  contractEndDate: string;
}

// ============================================
// Chat Types
// ============================================

export interface ChatChannel {
  id: number;
  orderId: number;
  supplierUserId: number;
  customerUserId: number;
  channelName: string;
  isActive: boolean;
  createdAt: string;
  messageCount: number;
  unreadCount: number;
  lastMessage?: ChatMessage;
}

export interface ChatMessage {
  id: number;
  channelId: number;
  senderId: number;
  messageText: string;
  messageType: 'TEXT' | 'ATTACHMENT';
  attachmentKey?: string;
  isRead: boolean;
  sentAt: string;
  readAt?: string;
}

export interface CreateChatChannelRequest {
  orderId: number;
  supplierUserId: number;
  customerUserId: number;
  channelName: string;
}

export interface SendMessageRequest {
  messageText: string;
  attachmentKey?: string;
}

// ============================================
// Notification Types
// ============================================

export interface OrderNotification {
  id: number;
  recipientId: number;
  type: string;
  typeDisplayName?: string;
  title?: string | null;
  message: string;
  orderId?: number | null;
  orderNumber?: string | null;
  read: boolean;
  createdAt: string;
  readAt?: string | null;
}

export interface UnreadCountResponse {
  unreadCount: number;
}

export interface UserNotification {
  id: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
  relatedEntityType?: string | null;
  relatedEntityId?: number | null;
}

// ============================================
// Support Ticket Types
// ============================================

export type TicketCategory =
  | 'TECHNICAL_ISSUE'
  | 'PAYMENT_ISSUE'
  | 'ORDER_ISSUE'
  | 'ACCOUNT_ISSUE'
  | 'VERIFICATION_ISSUE'
  | 'OTHER';

export type TicketPriority = 'LOW' | 'NORMAL' | 'HIGH';
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_USER' | 'RESOLVED' | 'CLOSED';

export interface SupportTicket {
  id: number;
  ticketNumber: string;
  companyId: number;
  userId: number;
  userEmail?: string;
  subject: string;
  category: TicketCategory;
  priority: TicketPriority;
  status: TicketStatus;
  assignedTo?: number;
  assignedAdminId?: number;
  assignedAdminEmail?: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  closedAt?: string;
  lastMessageAt?: string;
  lastMessage?: string;
  messageCount: number;
}

export interface CreateTicketRequest {
  subject: string;
  message: string;
  category: TicketCategory;
  priority: TicketPriority;
  attachmentKeys?: string[];
}

export interface TicketMessage {
  id: number;
  ticketId: number;
  senderId: number;
  senderEmail: string;
  senderRole: string;
  messageText: string;
  attachmentKeys?: string[];
  attachmentKey?: string;
  isInternalNote?: boolean;
  createdAt: string;
}

export interface SendTicketMessageRequest {
  message: string;
  attachmentKeys?: string[];
  attachmentKey?: string;
  isInternalNote?: boolean;
}

// ============================================
// Document Types
// ============================================

export type DocumentType =
  | 'TTN'
  | 'INVOICE'
  | 'DISCREPANCY_ACT'
  | 'PAYMENT_PROOF'
  | 'CONTRACT'
  | 'OTHER';

export type EntityType = 'ORDER' | 'COMPANY' | 'PRODUCT';

export interface Document {
  id: number;
  documentTypeCode?: string;
  documentTypeName?: string;
  fileName: string;
  fileType?: string;
  fileSize: number;
  mimeType?: string;
  documentType?: DocumentType;
  entityType: EntityType | string;
  entityId: number;
  uploadedBy?: number;
  createdAt?: string;
  uploadedAt?: string;
  fileKey?: string;
  documentKey?: string;
}

export interface DocumentTypeInfo {
  id: number;
  code: string;
  name: string;
  description?: string;
}

export interface GeneratedDocument {
  id: number;
  templateType: string;
  templateDisplayName: string;
  orderId: number;
  documentNumber: string;
  documentDate: string;
  fileKey: string;
  generatedAt: string;
  generatedBy?: number;
}

export interface GenerateTTNRequest {
  orderId: number;
}

export interface GenerateDiscrepancyActRequest {
  discrepancyReportId: number;
}

// ============================================
// Acceptance Journal Types
// ============================================

export interface AcceptanceJournalDetail {
  acceptanceDate: string;
  productId: number;
  productName: string;
  productSku: string;
  supplierId: number;
  supplierName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  orderId: number;
  orderNumber: string;
}

export interface AcceptanceJournalSummary {
  productId: number;
  productName: string;
  productSku: string;
  totalQuantity: number;
  totalAmount: number;
  uniqueSuppliers: number;
}

export interface AcceptanceJournal {
  details: AcceptanceJournalDetail[];
  summary: AcceptanceJournalSummary[];
  grandTotalQuantity: number;
  grandTotalAmount: number;
}

