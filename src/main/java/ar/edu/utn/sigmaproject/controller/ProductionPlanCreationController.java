package ar.edu.utn.sigmaproject.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Button;

import ar.edu.utn.sigmaproject.domain.Client;
import ar.edu.utn.sigmaproject.domain.Order;
import ar.edu.utn.sigmaproject.domain.OrderDetail;
import ar.edu.utn.sigmaproject.domain.Piece;
import ar.edu.utn.sigmaproject.domain.Process;
import ar.edu.utn.sigmaproject.domain.ProcessType;
import ar.edu.utn.sigmaproject.domain.Product;
import ar.edu.utn.sigmaproject.domain.ProductionPlan;
import ar.edu.utn.sigmaproject.domain.ProductionPlanDetail;
import ar.edu.utn.sigmaproject.service.ClientService;
import ar.edu.utn.sigmaproject.service.OrderDetailService;
import ar.edu.utn.sigmaproject.service.OrderService;
import ar.edu.utn.sigmaproject.service.OrderStateService;
import ar.edu.utn.sigmaproject.service.OrderStateTypeService;
import ar.edu.utn.sigmaproject.service.PieceService;
import ar.edu.utn.sigmaproject.service.ProcessService;
import ar.edu.utn.sigmaproject.service.ProcessTypeService;
import ar.edu.utn.sigmaproject.service.ProductService;
import ar.edu.utn.sigmaproject.service.ProductionPlanService;
import ar.edu.utn.sigmaproject.service.ProductionPlanDetailService;
import ar.edu.utn.sigmaproject.service.impl.ClientServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.OrderDetailServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.OrderServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.OrderStateServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.OrderStateTypeServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.PieceServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.ProcessServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.ProcessTypeServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.ProductServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.ProductionPlanServiceImpl;
import ar.edu.utn.sigmaproject.service.impl.ProductionPlanDetailServiceImpl;

public class ProductionPlanCreationController extends SelectorComposer<Component>{
	private static final long serialVersionUID = 1L;
	
	@Wire
    Listbox orderPopupListbox;
	@Wire
	Grid productionPlanDetailGrid;
	@Wire
	Bandbox orderBandbox;
	@Wire
    Datebox productionPlanDateBox;
	@Wire
	Button addOrderButton;
	@Wire
    Listbox processListbox;
	@Wire
    Listbox supplyListbox;
	@Wire
    Button resetProductionPlanButton;
    @Wire
    Button saveProductionPlanButton;
    @Wire
    Button deleteProductionPlanButton;
	
	// services
	private OrderService orderService = new OrderServiceImpl();
	private OrderDetailService orderDetailService = new OrderDetailServiceImpl();
	private OrderStateService orderStateService = new OrderStateServiceImpl();
	private OrderStateTypeService orderStateTypeService = new OrderStateTypeServiceImpl();
	private ProductService productService = new ProductServiceImpl();
	private ProductionPlanService productionPlanService = new ProductionPlanServiceImpl();
	private ProductionPlanDetailService productionPlanDetailService = new ProductionPlanDetailServiceImpl();
	private PieceService pieceService = new PieceServiceImpl();
	private ProcessService processService = new ProcessServiceImpl();
	private ProcessTypeService processTypeService = new ProcessTypeServiceImpl();
	private ClientService clientService = new ClientServiceImpl();
	
	// list
	private List<Order> orderPopupList;
    private List<ProductionPlanDetail> productionPlanDetailList;
	private List<ProductionPlanDetail> lateDeleteProductionPlanDetailList;
	
	// list models
	private ListModelList<Order> orderPopupListModel;
	private ListModelList<Process> processListModel;
	private ListModelList<ProductionPlanDetail> productionPlanDetailListModel;
    
    // atributes
    private Order currentOrder;
    private ProductionPlan currentProductionPlan;
    
	@Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        productionPlanDetailList = new ArrayList<ProductionPlanDetail>();
        refreshProductionPlanDetailListGrid();
        
        currentProductionPlan = (ProductionPlan) Executions.getCurrent().getAttribute("selected_production_plan");
        refreshViewProductionPlan();
        
        lateDeleteProductionPlanDetailList = new ArrayList<ProductionPlanDetail>();
        
        refreshProcessListBox();
    }
	
	@Listen("onSelect = #orderPopupListbox")
    public void selectionOrderPopupListbox() {
		currentOrder = (Order) orderPopupListbox.getSelectedItem().getValue();
		refreshOrder();
    }
	
	private void refreshOrder() {
		if(currentOrder != null) { 
			orderBandbox.setValue("Pedido nro " + currentOrder.getId());
			orderBandbox.close();
		}else {
			orderBandbox.setValue("");// borramos el text del producto  seleccionado
			orderPopupListbox.clearSelection();// deseleccionamos la tabla
		}
    }
	
	private void refreshOrderPopupList() {// el popup se actualiza en base a los detalles
		Integer order_state_type_id = orderStateTypeService.getOrderStateType("iniciado").getId();// se busca el id del estado de pedido iniciado
		orderPopupList = orderService.getOrderList(order_state_type_id);// se buscan los pedidos que no estan asignados a un plan y no estan cancelados (estan en estado iniciado)
    	for(ProductionPlanDetail productionPlanDetail : productionPlanDetailList) {// no debe contener los pedidos que ya estan en el detalle
    		Order aux = orderService.getOrder(productionPlanDetail.getIdOrder());
    		orderPopupList.remove(aux);// sacamos todos los pedidos del popup
    	}
    	orderPopupListModel = new ListModelList<Order>(orderPopupList);
        orderPopupListbox.setModel(orderPopupListModel);
	}
	
	@Listen("onClick = #resetProductionPlanButton")
    public void resetProductionPlan() {
		refreshViewProductionPlan();
    }
	
	@Listen("onClick = #addOrderButton")
    public void addOrder() {
		if(currentOrder == null) {
			Clients.showNotification("Debe seleccionar un pedido", orderBandbox);
			return;
		}
		// buscamos si el pedido no esta en un detalle eliminado
		ProductionPlanDetail aux = null;
		for(ProductionPlanDetail lateDeleteProductionPlanDetail : lateDeleteProductionPlanDetailList) {
			if(currentOrder.getId().equals(lateDeleteProductionPlanDetail.getIdOrder())) {
				aux = lateDeleteProductionPlanDetail;
			}
		}
		if(aux != null) {
			lateDeleteProductionPlanDetailList.remove(aux);// lo eliminamos de la lista de eliminacion tardia (porque el pedido sera agregado de nuevo, y no se eliminara tardiamente)
		}
		if(currentProductionPlan == null) {// es un plan de produccion nuevo
			productionPlanDetailList.add(new ProductionPlanDetail(null, currentOrder.getId()));
		} else {
			productionPlanDetailList.add(new ProductionPlanDetail(currentProductionPlan.getId(), currentOrder.getId()));
		}
		
		refreshProductionPlanDetailListGrid();
		currentOrder = null;
		
		refreshOrderPopupList();
		refreshOrder();
		refreshProcessListBox();
    }
	
	private void refreshProductionPlanDetailListGrid() {
		productionPlanDetailListModel = new ListModelList<ProductionPlanDetail>(productionPlanDetailList);
        productionPlanDetailGrid.setModel(productionPlanDetailListModel);
	}
	
	private void refreshViewProductionPlan() {
		currentOrder = null;// deseleccionamos el pedido
		refreshOrder();
  		if (currentProductionPlan == null) {// nuevo plan de produccion
  			productionPlanDateBox.setValue(new Date());
  	        productionPlanDetailList = new ArrayList<ProductionPlanDetail>();
  	        deleteProductionPlanButton.setDisabled(true);
  		} else {// se edita plan de produccion
  			productionPlanDateBox.setValue(currentProductionPlan.getDate());
  			productionPlanDetailList = productionPlanDetailService.getProductionPlanDetailList(currentProductionPlan.getId());
  			deleteProductionPlanButton.setDisabled(false);
  		}
  		refreshOrderPopupList();
  		refreshProductionPlanDetailListGrid();
  	}
    
	
	private void refreshProcessListBox() {
		List<Process> processList = new ArrayList<Process>();
		for(ProductionPlanDetail auxProductionPlanDetail : productionPlanDetailList) {
			Order auxOrder = orderService.getOrder(auxProductionPlanDetail.getIdOrder());
			List<OrderDetail> auxOrderDetailList = orderDetailService.getOrderDetailList(auxOrder.getId());
			for(OrderDetail auxOrderDetail : auxOrderDetailList) {
				Product auxProduct = productService.getProduct(auxOrderDetail.getIdProduct());
				List<Piece> auxPieceList = pieceService.getPieceList(auxProduct.getId());
				for(Piece auxPiece : auxPieceList) {
					List<Process> auxProcessList = processService.getProcessList(auxPiece.getId());
					for(Process auxProcess : auxProcessList) {
						processList.add(auxProcess);
					}
				}
			}
		}
		processListModel = new ListModelList<Process>(processList);
        processListbox.setModel(processListModel);
	}
	
	public String getClientName(int idClient) {
		Client aux = clientService.getClient(idClient);
		return aux.getName();
    }
	
	public String getClientNameByOrderId(int idOrder) {
		Order auxOrder = orderService.getOrder(idOrder);
		Client auxClient = clientService.getClient(auxOrder.getIdClient());
		return auxClient.getName();
    }
	
	public String getProductName(int idProduct) {
		Product aux = productService.getProduct(idProduct);
		return aux.getName();
    }
	
	public String getProductUnits(int idProduct) {
		return "[Impl Pend]";
    }
	
	public String getProductTotalPrice(int idProduct) {
		return "[Impl Pend]";
    }
	
	public String getProductNameByPieceId(int idPiece) {
		Piece aux = pieceService.getPiece(idPiece);
		Product aux2 = productService.getProduct(aux.getIdProduct());
		return aux2.getName();
    }
	
	public String getPieceName(int idPiece) {
		Piece aux = pieceService.getPiece(idPiece);
		return aux.getName();
    }
	
	public String getProcessName(int idProcessType) {
		ProcessType aux = processTypeService.getProcessType(idProcessType);
		return aux.getName();
    }
	
	public String getPieceUnits(int idPiece) {
		Piece aux = pieceService.getPiece(idPiece);
		return aux.getUnits() + "";
    }
	
	public String getPieceTotalUnits(int idPiece) {
		Piece auxPiece = pieceService.getPiece(idPiece);
		int units = 0;
		for(ProductionPlanDetail auxProductionPlanDetail : productionPlanDetailList) {
			Order auxOrder = orderService.getOrder(auxProductionPlanDetail.getIdOrder());
			List<OrderDetail> auxOrderDetailList = orderDetailService.getOrderDetailList(auxOrder.getId());
			for(OrderDetail auxOrderDetail : auxOrderDetailList) {
				if(auxOrderDetail.getIdProduct().equals(auxPiece.getIdProduct())) {
					units = auxOrderDetail.getUnits();
				}
			}
		}
		if(units > 0) {
			units = auxPiece.getUnits() * units;
		}
		return units + "";
    }
	
	public String getTotalTime(int idPiece, int idProcessType) {
		Process auxProcess = processService.getProcess(idPiece, idProcessType);
		Piece auxPiece = pieceService.getPiece(idPiece);
		long total = 0;
		int units = 0;
		for(ProductionPlanDetail auxProductionPlanDetail : productionPlanDetailList) {
			Order auxOrder = orderService.getOrder(auxProductionPlanDetail.getIdOrder());
			List<OrderDetail> auxOrderDetailList = orderDetailService.getOrderDetailList(auxOrder.getId());
			for(OrderDetail auxOrderDetail : auxOrderDetailList) {
				if(auxOrderDetail.getIdProduct().equals(auxPiece.getIdProduct())) {
					units = auxOrderDetail.getUnits();
				}
			}
		}
		if(units > 0) {
			total = auxProcess.getTime().getMinutes() * units;
		}
		return total + "";
    }
	
	public String quantityOfDetail(int idOrder) {
    	return orderDetailService.getOrderDetailList(idOrder).size() + "";
    }
	
}
