/*
 * The MIT License
 *
 * Copyright (C) 2017 SigmaProject.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ar.edu.utn.sigmaproject.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Area;
import org.zkoss.zul.Button;
import org.zkoss.zul.Chart;
import org.zkoss.zul.GanttModel;
import org.zkoss.zul.GanttModel.GanttTask;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

import ar.edu.utn.sigmaproject.domain.ProcessState;
import ar.edu.utn.sigmaproject.domain.ProductionOrder;
import ar.edu.utn.sigmaproject.domain.ProductionOrderDetail;
import ar.edu.utn.sigmaproject.util.GanttChartEngine;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ProductionOrderGanttController extends SelectorComposer<Component> {
	private static final long serialVersionUID = 1L;

	@Wire
	Chart ganttChart;
	@Wire
	Button cancelButton;
	@Wire
	Window win_production_order_gantt;

	// services

	// atributes
	private ProductionOrder currentProductionOrder;

	// list

	// list models

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		currentProductionOrder = (ProductionOrder) Executions.getCurrent().getAttribute("selected_production_order");
		if(currentProductionOrder == null) {throw new RuntimeException("ProductionOrder not found");}

		ganttChart.setType("gantt");
		ganttChart.setEngine(new GanttChartEngine());
		ganttChart.setModel(getModel());
		
		String title = currentProductionOrder.getProductionPlan().getName() + ", Orden: " + currentProductionOrder.getNumber() + ", Producto: " + currentProductionOrder.getProduct().getDescription();
		win_production_order_gantt.setTitle(title);
	}

	@Listen("onClick = #cancelButton")
	public void cancelButtonClick() {
		Include include = (Include) Selectors.iterable(this.getPage(), "#mainInclude").iterator().next();
		include.setSrc("/production_follow_up_list.zul");
	}

	@Listen("onClick = #ganttChart")
	public void ganttChartOnClick(MouseEvent event) {
		Area area = (Area) event.getAreaComponent();
		alert(area.getTooltiptext());
	}

	public GanttModel getModel() {
		GanttModel ganttmodel = new GanttModel();
		for(ProductionOrderDetail each : currentProductionOrder.getDetails()) {
			if(each.getState() != ProcessState.Cancelado) {
				ganttmodel.addValue("Programado", new GanttTask(each.getProcess().getType().getName(), each.getDateStart(), each.getDateFinish(), 0.0));
				if(each.getDateStartReal() != null && each.getDateFinishReal() != null) {
					ganttmodel.addValue("Real", new GanttTask(each.getProcess().getType().getName(), each.getDateStartReal(), each.getDateFinishReal(), 0.0));
				}
			}
		}
		return ganttmodel;
	}

}