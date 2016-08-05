package ar.edu.utn.sigmaproject.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

import ar.edu.utn.sigmaproject.domain.Order;
import ar.edu.utn.sigmaproject.domain.OrderDetail;
import ar.edu.utn.sigmaproject.domain.OrderState;
import ar.edu.utn.sigmaproject.domain.OrderStateType;
import ar.edu.utn.sigmaproject.domain.Piece;
import ar.edu.utn.sigmaproject.domain.Process;
import ar.edu.utn.sigmaproject.domain.ProcessType;
import ar.edu.utn.sigmaproject.domain.Product;
import ar.edu.utn.sigmaproject.domain.ProductTotal;
import ar.edu.utn.sigmaproject.domain.ProductionOrder;
import ar.edu.utn.sigmaproject.domain.ProductionOrderState;
import ar.edu.utn.sigmaproject.domain.ProductionPlan;
import ar.edu.utn.sigmaproject.domain.ProductionPlanDetail;
import ar.edu.utn.sigmaproject.domain.ProductionPlanState;
import ar.edu.utn.sigmaproject.domain.ProductionPlanStateType;
import ar.edu.utn.sigmaproject.domain.RawMaterial;
import ar.edu.utn.sigmaproject.domain.RawMaterialRequirement;
import ar.edu.utn.sigmaproject.domain.Supply;
import ar.edu.utn.sigmaproject.domain.SupplyRequirement;
import ar.edu.utn.sigmaproject.service.ClientRepository;
import ar.edu.utn.sigmaproject.service.OrderRepository;
import ar.edu.utn.sigmaproject.service.OrderStateRepository;
import ar.edu.utn.sigmaproject.service.OrderStateTypeRepository;
import ar.edu.utn.sigmaproject.service.ProductRepository;
import ar.edu.utn.sigmaproject.service.ProductionOrderRepository;
import ar.edu.utn.sigmaproject.service.ProductionOrderStateRepository;
import ar.edu.utn.sigmaproject.service.ProductionOrderStateTypeRepository;
import ar.edu.utn.sigmaproject.service.ProductionPlanDetailRepository;
import ar.edu.utn.sigmaproject.service.ProductionPlanRepository;
import ar.edu.utn.sigmaproject.service.ProductionPlanStateRepository;
import ar.edu.utn.sigmaproject.service.ProductionPlanStateTypeRepository;
import ar.edu.utn.sigmaproject.service.RawMaterialRequirementRepository;
import ar.edu.utn.sigmaproject.service.SupplyRequirementRepository;

public class ProductionPlanCreationController extends SelectorComposer<Component> {
	private static final long serialVersionUID = 1L;

	@Wire
	Listbox orderPopupListbox;
	@Wire
	Grid productionPlanDetailGrid;
	@Wire
	Bandbox orderBandbox;
	@Wire
	Textbox productionPlanNameTextbox;
	@Wire
	Button addOrderButton;
	@Wire
	Button resetProductionPlanButton;
	@Wire
	Button saveProductionPlanButton;
	@Wire
	Button deleteProductionPlanButton;
	@Wire
	Listbox productTotalListbox;
	@Wire
	Combobox productionPlanStateTypeCombobox;
	@Wire
	Caption productionPlanCaption;

	// services
	@WireVariable
	private OrderRepository orderRepository;
	@WireVariable
	private OrderStateRepository orderStateRepository;
	@WireVariable
	private OrderStateTypeRepository orderStateTypeRepository;
	@WireVariable
	private ProductRepository productRepository;
	@WireVariable
	private ProductionPlanRepository productionPlanRepository;
	@WireVariable
	private ProductionPlanDetailRepository productionPlanDetailRepository;
	@WireVariable
	private ClientRepository clientService;
	@WireVariable
	private ProductionPlanStateRepository productionPlanStateRepository;
	@WireVariable
	private ProductionPlanStateTypeRepository productionPlanStateTypeRepository;
	@WireVariable
	private SupplyRequirementRepository supplyRequirementRepository;
	@WireVariable
	private RawMaterialRequirementRepository rawMaterialRequirementRepository;
	@WireVariable
	private ProductionOrderRepository productionOrderRepository;
	@WireVariable
	private ProductionOrderStateRepository productionOrderStateRepository;
	@WireVariable
	private ProductionOrderStateTypeRepository productionOrderStateTypeRepository;

	// list
	private List<Order> orderPopupList;
	private List<ProductionPlanDetail> currentProductionPlanDetailList;
	private List<ProductTotal> productTotalList;
	private List<ProductionPlanStateType> productionPlanStateTypeList;

	// list models
	private ListModelList<Order> orderPopupListModel;
	private ListModelList<ProductionPlanDetail> productionPlanDetailListModel;
	private ListModelList<ProductionPlanStateType> productionPlanStateTypeListModel;

	// atributes
	private Order currentOrder;
	private ProductionPlan currentProductionPlan;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		productTotalList = new ArrayList<ProductTotal>();
		currentProductionPlanDetailList = new ArrayList<ProductionPlanDetail>();
		currentProductionPlan = (ProductionPlan) Executions.getCurrent().getAttribute("selected_production_plan");

		productionPlanStateTypeList = productionPlanStateTypeRepository.findAll();
		productionPlanStateTypeListModel = new ListModelList<ProductionPlanStateType>(productionPlanStateTypeList);
		productionPlanStateTypeCombobox.setModel(productionPlanStateTypeListModel);

		refreshViewProductionPlan();
		refreshProductTotalListbox();
	}

	@Listen("onSelect = #orderPopupListbox")
	public void selectionOrderPopupListbox() {
		currentOrder = (Order) orderPopupListbox.getSelectedItem().getValue();
		refreshOrder();
	}

	private void refreshOrder() {
		if(currentOrder != null) {
			orderBandbox.setValue("Pedido " + currentOrder.getId());
			orderBandbox.close();
		}else {
			orderBandbox.setValue("");// borramos el text del producto  seleccionado
			orderPopupListbox.clearSelection();// deseleccionamos la tabla
		}
	}

	private void refreshOrderPopupList() {// el popup se actualiza en base a los detalles
		List<Order> orderList = orderRepository.findAll();
		orderPopupList = new ArrayList<>();
		// se buscan los pedidos que no estan asignados a un plan y no estan cancelados (estan en estado iniciado)
		OrderStateType orderStateTypeInitiated = orderStateTypeRepository.findFirstByName("Iniciado");
		for(Order each : orderList) {
			if(each.getCurrentStateType().equals(orderStateTypeInitiated)) {
				orderPopupList.add(each);
			}
		}
		for(ProductionPlanDetail productionPlanDetail : currentProductionPlanDetailList) {
			// no debe contener los pedidos que ya estan en el detalle
			Order aux = productionPlanDetail.getOrder();
			orderPopupList.remove(aux);
		}
		orderPopupListModel = new ListModelList<Order>(orderPopupList);
		orderPopupListbox.setModel(orderPopupListModel);
	}

	@Listen("onClick = #resetProductionPlanButton")
	public void resetProductionPlan() {
		refreshViewProductionPlan();
	}

	@Listen("onClick = #deleteProductionPlanButton")
	public void deleteProductionPlan() {
		if(currentProductionPlan != null) {
			productionPlanRepository.delete(currentProductionPlan);
			currentProductionPlan = null;
			refreshViewProductionPlan();
		}
	}

	@Transactional
	@Listen("onClick = #saveProductionPlanButton")
	public void saveProductionPlan() {
		if(currentProductionPlanDetailList.size() == 0) {
			Clients.showNotification("Ingresar al menos 1 pedido", addOrderButton);
			return;
		}
		String productionPlanName = productionPlanNameTextbox.getText().toUpperCase();
		ProductionPlanStateType productionPlanStateType;
		if(productionPlanStateTypeCombobox.getSelectedIndex() == -1) {
			productionPlanStateType = null;
		} else {
			productionPlanStateType = productionPlanStateTypeCombobox.getSelectedItem().getValue();
		}
		boolean isNewProductionPlan = false;
		currentProductionPlanDetailList = productionPlanDetailRepository.save(currentProductionPlanDetailList);
		if(currentProductionPlan == null) { // es un plan nuevo
			// creamos el nuevo plan
			currentProductionPlan = new ProductionPlan(productionPlanName, currentProductionPlanDetailList);
			ProductionPlanState productionPlanState = new ProductionPlanState(productionPlanStateType, new Date());
			productionPlanState = productionPlanStateRepository.save(productionPlanState);
			currentProductionPlan.setState(productionPlanState);
			// cambia el estado de los pedidos
			for(ProductionPlanDetail each : currentProductionPlanDetailList) {
				OrderStateType orderStateType = orderStateTypeRepository.findFirstByName("Planificado");
				Order order = each.getOrder();
				OrderState state = new OrderState(orderStateType, new Date());
				state = orderStateRepository.save(state);
				order.setState(state);
				orderRepository.save(order);
			}
			isNewProductionPlan = true;
		} else { // se edita un plan
			currentProductionPlan.setName(productionPlanName);
			currentProductionPlan.setPlanDetails(currentProductionPlanDetailList);
			if (!currentProductionPlan.getCurrentStateType().equals(productionPlanStateType)) {
				// si el estado ha cambiado
				ProductionPlanState productionPlanState = new ProductionPlanState(productionPlanStateType, new Date());
				productionPlanState = productionPlanStateRepository.save(productionPlanState);
				currentProductionPlan.setState(productionPlanState);
			}
		}

		currentProductionPlan = productionPlanRepository.save(currentProductionPlan);
		if(isNewProductionPlan) {
			// crea los requerimientos
			List<SupplyRequirement> supplyRequirementList = createSupplyRequirements(currentProductionPlan);
			supplyRequirementList = supplyRequirementRepository.save(supplyRequirementList);
			currentProductionPlan.getSupplyRequirements().addAll(supplyRequirementList);
			List<RawMaterialRequirement> rawMaterialRequirementList = createRawMaterialRequirements(currentProductionPlan);
			rawMaterialRequirementList = rawMaterialRequirementRepository.save(rawMaterialRequirementList);
			currentProductionPlan.getRawMaterialRequirements().addAll(rawMaterialRequirementList);
			// crea ordenes de produccion
			for(ProductTotal each : currentProductionPlan.getProductTotalList()) {
				ProductionOrderState productionOrderState = new ProductionOrderState(productionOrderStateTypeRepository.findFirstByName("Generada"), new Date());
				productionOrderState = productionOrderStateRepository.save(productionOrderState);
				ProductionOrder productionOrder = new ProductionOrder(currentProductionPlan, each.getProduct(), null, null, each.getTotalUnits(), null, null, productionOrderState);
				productionOrder = productionOrderRepository.save(productionOrder);
			}
			currentProductionPlan = productionPlanRepository.save(currentProductionPlan);
		}
		refreshViewProductionPlan();
		alert("Plan guardado.");
	}

	private List<SupplyRequirement> createSupplyRequirements(ProductionPlan productionPlan) {
		// busca los requerimientos
		List<SupplyRequirement> list = new ArrayList<>();
		List<ProductTotal> productTotalList = productionPlan.getProductTotalList();
		for (ProductTotal productTotal : productTotalList) {
			for (Supply supply : productTotal.getProduct().getSupplies()) {
				SupplyRequirement auxSupplyRequirement = null;
				for (SupplyRequirement supplyRequirement : list) {// busca si el insumo no se encuentra agregado
					if (supply.getSupplyType().equals(supplyRequirement.getSupplyType())) {
						auxSupplyRequirement = supplyRequirement;
					}
				}
				if (auxSupplyRequirement != null) {// el insumo si se encuentra agregado, suma sus cantidades
					auxSupplyRequirement.setQuantity(auxSupplyRequirement.getQuantity().add(supply.getQuantity()));
				} else {// el insumo no se encuentra, se lo agrega
					list.add(new SupplyRequirement(supply.getSupplyType(), supply.getQuantity()));
				}
			}
		}
		return list;
	}

	private List<RawMaterialRequirement> createRawMaterialRequirements(ProductionPlan productionPlan) {
		// busca requerimientos de materias primas
		List<RawMaterialRequirement> list = new ArrayList<RawMaterialRequirement>();
		List<ProductTotal> productTotalList = productionPlan.getProductTotalList();
		for(ProductTotal productTotal : productTotalList) {
			Product product = productTotal.getProduct();
			for(RawMaterial rawMaterial : product.getRawMaterials()) {
				RawMaterialRequirement auxRawMaterialRequirement = null;
				for(RawMaterialRequirement supplyRequirement : list) {// buscamos si la materia prima no se encuentra agregada
					if(rawMaterial.getRawMaterialType().equals(supplyRequirement.getRawMaterialType())) {
						auxRawMaterialRequirement = supplyRequirement;
					}
				}
				if(auxRawMaterialRequirement != null) {// la materia prima si se encuentra agregada, sumamos sus cantidades
					auxRawMaterialRequirement.setQuantity(auxRawMaterialRequirement.getQuantity().add(rawMaterial.getQuantity()));
				} else {// la materia prima no se encuentra, se la agrega
					list.add(new RawMaterialRequirement(rawMaterial.getRawMaterialType(), rawMaterial.getQuantity()));
				}
			}
		}
		return list;
	}

	@Listen("onClick = #addOrderButton")
	public void addOrder() {
		currentProductionPlanDetailList.add(new ProductionPlanDetail(currentOrder));
		refreshProductionPlanDetailListGrid();
		currentOrder = null;
		refreshOrderPopupList();
		refreshOrder();
		refreshProductTotalListbox();
	}

	@Listen("onRemoveOrder = #productionPlanDetailGrid")
	public void doRemoveOrder(ForwardEvent evt) {
		Order order = (Order) evt.getData();
		// eliminamos el detalle de la lista
		for(ProductionPlanDetail auxProductionPlanDetail : currentProductionPlanDetailList) {
			if (auxProductionPlanDetail.getOrder().equals(order)) {
				currentProductionPlanDetailList.remove(auxProductionPlanDetail);
				break;
			}
		}
		refreshProductionPlanDetailListGrid();
		refreshProductTotalListbox();
		refreshOrderPopupList();
	}

	private void refreshProductionPlanDetailListGrid() {
		productionPlanDetailListModel = new ListModelList<ProductionPlanDetail>(currentProductionPlanDetailList);
		productionPlanDetailGrid.setModel(productionPlanDetailListModel);
	}

	private void refreshProductTotalListbox() {
		refreshProductTotalList();
		ListModelList<ProductTotal> productTotalListModel = new ListModelList<ProductTotal>(productTotalList);
		productTotalListbox.setModel(productTotalListModel);
	}

	private void refreshViewProductionPlan() {
		currentOrder = null;// deseleccionamos el pedido
		refreshOrder();
		if (currentProductionPlan == null) {// nuevo plan de produccion
			productionPlanCaption.setLabel("Creacion de Plan de Produccion");
			productionPlanStateTypeListModel.addToSelection(productionPlanStateTypeRepository.findFirstByName("Iniciado"));
			productionPlanStateTypeCombobox.setModel(productionPlanStateTypeListModel);
			productionPlanNameTextbox.setText("");
			currentProductionPlanDetailList = new ArrayList<ProductionPlanDetail>();
			deleteProductionPlanButton.setDisabled(true);
			productionPlanStateTypeCombobox.setDisabled(true);
		} else {// se edita plan de produccion
			productionPlanCaption.setLabel("Edicion de Plan de Produccion");
			if (currentProductionPlan.getCurrentStateType() != null) {
				productionPlanStateTypeListModel.addToSelection(currentProductionPlan.getCurrentStateType());
				productionPlanStateTypeCombobox.setModel(productionPlanStateTypeListModel);
			} else {
				productionPlanStateTypeCombobox.setSelectedIndex(-1);
			}
			if(currentProductionPlan.getName() != null) {
				productionPlanNameTextbox.setText(currentProductionPlan.getName());
			} else {
				productionPlanNameTextbox.setText("");
			}
			currentProductionPlanDetailList = currentProductionPlan.getPlanDetails();
			deleteProductionPlanButton.setDisabled(false);
			productionPlanStateTypeCombobox.setDisabled(false);
		}
		refreshOrderPopupList();
		refreshProductionPlanDetailListGrid();
		refreshProductTotalListbox();
	}

	private void refreshProductTotalList() {
		Map<Product, Integer> productTotalMap = new HashMap<Product, Integer>();
		for(ProductionPlanDetail auxProductionPlanDetail : currentProductionPlanDetailList) {
			for(OrderDetail auxOrderDetail : auxProductionPlanDetail.getOrder().getDetails()) {
				Integer totalUnits = productTotalMap.get(auxOrderDetail.getProduct());
				productTotalMap.put(auxOrderDetail.getProduct(), (totalUnits == null) ? auxOrderDetail.getUnits() : totalUnits + auxOrderDetail.getUnits());
			}
		}
		productTotalList = new ArrayList<ProductTotal>();
		for (Map.Entry<Product, Integer> entry : productTotalMap.entrySet()) {
			Product product = entry.getKey();
			Integer totalUnits = entry.getValue();
			ProductTotal productTotal = new ProductTotal(product, totalUnits);
			productTotalList.add(productTotal);
		}
	}

	public int getProductTotalUnits(Product product) {
		int productTotalUnits = 0;
		for(ProductTotal productTotal : productTotalList) {// buscamos el total de unidades
			if(productTotal.getProduct().equals(product)) {
				productTotalUnits = productTotal.getTotalUnits();
			}
		}
		return productTotalUnits;
	}

	public BigDecimal getProductTotalPrice(Product product) {
		int productTotalUnits = getProductTotalUnits(product);
		return getTotalPrice(productTotalUnits, product.getPrice());// esta funcion es incorrecta pq agarra el valor actual del producto cuando deberia ser el valor en el pedido
	}

	private BigDecimal getTotalPrice(int units, BigDecimal price) {
		if(price == null) {
			price = BigDecimal.ZERO;
		}
		return price.multiply(new BigDecimal(units));
	}

	public String getPieceTotalUnits(Piece piece) {
		int units = 0;
		for(ProductionPlanDetail auxProductionPlanDetail : currentProductionPlanDetailList) {
			for (OrderDetail auxOrderDetail : auxProductionPlanDetail.getOrder().getDetails()) {
				if (auxOrderDetail.getProduct().equals(productRepository.findByPieces(piece))) {
					units = auxOrderDetail.getUnits();
				}
			}
		}
		if(units > 0) {
			units = piece.getUnits() * units;
		}
		return units + "";
	}

	public String getTotalTime(Piece piece, ProcessType processType) {
		Process process = null;
		for(int j = 0; j < piece.getProcesses().size(); j++) {
			if (piece.getProcesses().get(j).getType().equals(processType)) {
				process = piece.getProcesses().get(j);
				break;
			}
		}
		long total = 0;
		int units = 0;
		for (ProductionPlanDetail productionPlanDetail : currentProductionPlanDetailList) {
			for (OrderDetail auxOrderDetail : productionPlanDetail.getOrder().getDetails()) {
				if (auxOrderDetail.getProduct().equals(productRepository.findByPieces(piece))) {
					units = auxOrderDetail.getUnits();
				}
			}
		}
		if(units > 0) {
			total = process.getTime().getMinutes() * units;
		}
		return total + "";
	}

}
