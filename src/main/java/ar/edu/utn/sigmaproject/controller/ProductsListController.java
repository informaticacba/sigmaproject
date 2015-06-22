package ar.edu.utn.sigmaproject.controller;

import ar.edu.utn.sigmaproject.domain.Product;
import ar.edu.utn.sigmaproject.service.ProductListService;

import ar.edu.utn.sigmaproject.service.impl.ProductListServiceImpl;

//import ar.edu.utn.sigmaproject.util.SortingPagingHelper;
//import java.util.LinkedHashMap;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.zkoss.zk.ui.select.annotation.WireVariable;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventQueues;
//import org.zkoss.zk.ui.select.annotation.VariableResolver;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Textbox;


import java.util.List;

public class ProductsListController extends SelectorComposer<Component>{
	private static final long serialVersionUID = 1L;
	
	@Wire
    Textbox searchTextbox;
    @Wire
    Listbox productListbox;
    @Wire
    Paging pager;
    
    ProductListService productListService = new ProductListServiceImpl();
	
    ListModelList<Product> productListModel;
	//private String query;
    //private SortingPagingHelper<Product> sortingPagingHelper;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        //LinkedHashMap<String, Boolean> sortProperties = new LinkedHashMap<String, Boolean>();
        //sortProperties.put("firstName", Boolean.TRUE);
        //sortProperties.put("lastName", Boolean.TRUE);
        //sortingPagingHelper = new SortingPagingHelper<Product>(clientListbox, pager, sortProperties, this, 1);
        List<Product> productList = productListService.getProductList();
        productListModel = new ListModelList<Product>(productList);
        productListbox.setModel(productListModel);
    }
    
    @Listen("onSelect = #productListbox")
    public void onClientSelect() {
        //EventQueues.lookup(SELECT_CLIENT_QUEUE_NAME).publish(new Event(SELECT_CLIENT_EVENT_NAME, null, clientListbox.getSelectedItem().getValue()));
        getSelf().detach();
    }
    
    @Listen("onClick = #searchButton")
    public void search() {
        //query = searchTextbox.getValue();
        //sortingPagingHelper.resetUnsorted();
    }
    
    /*
    @Override
    public Page<Product> getPageForPageRequest(PageRequest pageRequest) {
        Page<Product> results;
        if (query != null && !query.isEmpty()) {
            results = clientService.getClients(query, pageRequest);
        } else {
            results = clientService.getClients(pageRequest);
        }
        return results;
    }
	*/
}
