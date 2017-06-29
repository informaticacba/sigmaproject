package ar.edu.utn.sigmaproject.controller;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Window;

import ar.edu.utn.sigmaproject.domain.Item;
import ar.edu.utn.sigmaproject.domain.MaterialType;
import ar.edu.utn.sigmaproject.domain.Product;
import ar.edu.utn.sigmaproject.domain.ProductMaterial;
import ar.edu.utn.sigmaproject.domain.Wood;
import ar.edu.utn.sigmaproject.service.WoodRepository;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ProductRawMaterialController extends ProductMaterialController {
	private static final long serialVersionUID = 1L;

	@Wire
	Window productRawMaterialWindow;

	// services
	@WireVariable
	private WoodRepository woodRepository;

	@SuppressWarnings("unchecked")
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		productMaterialList = (List<ProductMaterial>) Executions.getCurrent().getAttribute("rawMaterialList");
		currentProduct = (Product) Executions.getCurrent().getAttribute("currentProduct");
		currentProductMaterial = null;
		currentMaterial = null;
		refreshView();
		refreshMaterialPopup();
	}

	@Listen("onClick = #acceptProductMaterialButton")
	public void acceptProductMaterialButtonClick() {
		EventQueue<Event> eq = EventQueues.lookup("Product Change Queue", EventQueues.DESKTOP, true);
		eq.publish(new Event("onRawMaterialChange", null, productMaterialList));
		productRawMaterialWindow.detach();
	}

	@Listen("onClick = #cancelProductMaterialButton")
	public void cancelProductMaterialButtonClick() {
		productRawMaterialWindow.detach();
	}

	@Listen("onSelect = #materialPopupListbox")
	public void materialPopupListboxSelect() {
		currentMaterial = (Wood) materialPopupListbox.getSelectedItem().getValue();
		materialBandbox.setValue(((Wood)currentMaterial).getName());
		materialBandbox.close();
		materialQuantityDoublebox.setFocus(true);
	}

	@Override
	protected void refreshView() {
		productMaterialListModel = new ListModelList<>(productMaterialList);
		productMaterialListbox.setModel(productMaterialListModel);
		if (currentProductMaterial == null) {
			// borramos el text de la materia prima
			// deseleccionamos la tabla y borramos la cantidad
			materialBandbox.setDisabled(false);
			materialBandbox.setValue("");
			materialQuantityDoublebox.setValue(null);
			currentMaterial = null;
			deleteMaterialButton.setDisabled(true);
			cancelMaterialButton.setDisabled(true);
		} else {
			currentMaterial = currentProductMaterial.getItem();
			materialBandbox.setDisabled(true);// no se permite modificar en la edicion
			materialBandbox.setValue(((Wood)currentMaterial).getName());
			materialQuantityDoublebox.setValue(currentProductMaterial.getQuantity().doubleValue());
			deleteMaterialButton.setDisabled(false);
			cancelMaterialButton.setDisabled(false);
		}
	}

	@Override
	protected void refreshMaterialPopup() {// el popup se actualiza en base a la lista
		materialPopupListbox.clearSelection();
		materialPopupList = new ArrayList<Item>();
		materialPopupList.addAll(woodRepository.findAll());
		for(ProductMaterial rawMaterial : productMaterialList) {
			materialPopupList.remove(woodRepository.findOne(rawMaterial.getItem().getId()));// sacamos del popup
		}
		materialPopupListModel = new ListModelList<>(materialPopupList);
		materialPopupListbox.setModel(materialPopupListModel);
	}

	@Override
	protected void filterItems() {
		List<Item> someItems = new ArrayList<>();
		String textFilter = materialBandbox.getValue().toLowerCase();
		for(Item each : materialPopupList) {
			Wood eachWood = (Wood) each;
			if((eachWood.getFormattedMeasure()+eachWood.getName()).toLowerCase().contains(textFilter) || textFilter.equals("")) {
				someItems.add(each);
			}
		}
		materialPopupListModel = new ListModelList<>(someItems);
		materialPopupListbox.setModel(materialPopupListModel);
	}

	@Override
	protected MaterialType getMaterialType() {
		return MaterialType.Wood;
	}
}
