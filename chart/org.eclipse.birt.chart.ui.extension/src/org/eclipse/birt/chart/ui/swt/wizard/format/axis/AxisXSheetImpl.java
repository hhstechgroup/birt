/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Actuate Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.birt.chart.ui.swt.wizard.format.axis;

import java.util.List;

import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AngleType;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.ui.extension.i18n.Messages;
import org.eclipse.birt.chart.ui.swt.composites.ExternalizedTextEditorComposite;
import org.eclipse.birt.chart.ui.swt.wizard.format.SubtaskSheetImpl;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.axis.AxisGridLinesSheet;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.axis.AxisMarkersSheet;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.axis.AxisTextSheet;
import org.eclipse.birt.chart.ui.swt.wizard.format.popup.axis.MoreOptionsAxisXSheet;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Actuate Corporation
 * 
 */
public class AxisXSheetImpl extends SubtaskSheetImpl
		implements
			Listener,
			SelectionListener
{

	private transient Composite cmpContent = null;

	private transient ExternalizedTextEditorComposite txtTitle;

	private transient Button btnVisible;

	private transient Button btnAxisTitle;

	private transient Button btnGridlines;

	private transient Button btnMarkers;

	private transient Button btnMoreOpt;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.ui.swt.ISheet#getComponent(org.eclipse.swt.widgets.Composite)
	 */
	public void getComponent( Composite parent )
	{
		cmpContent = new Composite( parent, SWT.NONE );
		{
			GridLayout glContent = new GridLayout( 2, true );
			cmpContent.setLayout( glContent );
			GridData gd = new GridData( GridData.FILL_BOTH );
			cmpContent.setLayoutData( gd );
		}

		Composite cmpBasic = new Composite( cmpContent, SWT.NONE );
		{
			cmpBasic.setLayout( new GridLayout( 2, false ) );
			cmpBasic.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		}

		new Label( cmpBasic, SWT.NONE ).setText( Messages.getString( "BaseAxisDataSheetImpl.Lbl.Title" ) ); //$NON-NLS-1$

		List keys = null;
		if ( serviceprovider != null )
		{
			keys = serviceprovider.getRegisteredKeys( );
		}

		txtTitle = new ExternalizedTextEditorComposite( cmpBasic,
				SWT.BORDER | SWT.SINGLE,
				-1,
				-1,
				keys,
				serviceprovider,
				getAxisForProcessing( ).getTitle( ).getCaption( ).getValue( ) );
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			txtTitle.setLayoutData( gd );
			txtTitle.addListener( this );
		}

		new Label( cmpBasic, SWT.NONE ).setText( Messages.getString( "AxisXSheetImpl.Label.Labels" ) ); //$NON-NLS-1$

		btnVisible = new Button( cmpBasic, SWT.CHECK );
		{
			btnVisible.setText( Messages.getString( "AxisXSheetImpl.Label.Visible" ) ); //$NON-NLS-1$
			btnVisible.addSelectionListener( this );
			btnVisible.setSelection( getAxisForProcessing( ).getLabel( )
					.isVisible( ) );
		}

		createButtonGroup( cmpContent );
	}

	private void createButtonGroup( Composite parent )
	{
		Composite cmp = new Composite( parent, SWT.NONE );
		{
			cmp.setLayout( new GridLayout( 5, true ) );
			GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
			gridData.horizontalSpan = 2;
			gridData.grabExcessVerticalSpace = true;
			gridData.verticalAlignment = SWT.END;
			cmp.setLayoutData( gridData );
		}

		btnAxisTitle = new Button( cmp, SWT.TOGGLE );
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			btnAxisTitle.setLayoutData( gd );
			btnAxisTitle.setText( Messages.getString( "AxisXSheetImpl.Label.AxisText" ) ); //$NON-NLS-1$
			btnAxisTitle.addSelectionListener( this );
		}

		btnGridlines = new Button( cmp, SWT.TOGGLE );
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			btnGridlines.setLayoutData( gd );
			btnGridlines.setText( Messages.getString( "AxisXSheetImpl.Label.Gridlines" ) ); //$NON-NLS-1$
			btnGridlines.addSelectionListener( this );
		}

		btnMarkers = new Button( cmp, SWT.TOGGLE );
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			btnMarkers.setLayoutData( gd );
			btnMarkers.setText( Messages.getString( "AxisXSheetImpl.Label.Markers" ) ); //$NON-NLS-1$
			btnMarkers.addSelectionListener( this );
		}

		btnMoreOpt = new Button( cmp, SWT.TOGGLE );
		{
			GridData gd = new GridData( GridData.FILL_HORIZONTAL );
			btnMoreOpt.setLayoutData( gd );
			btnMoreOpt.setText( Messages.getString( "AxisXSheetImpl.Label.MoreOptions" ) ); //$NON-NLS-1$
			btnMoreOpt.addSelectionListener( this );
		}
	}

	public Object onHide( )
	{
		detachPopup( );
		cmpContent.dispose( );
		return getContext( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent( Event event )
	{
		if ( event.widget.equals( txtTitle ) )
		{
			getAxisForProcessing( ).getTitle( )
					.getCaption( )
					.setValue( (String) event.data );
		}
	}

	public void widgetSelected( SelectionEvent e )
	{
		// detach popup dialogue
		if ( detachPopup( e.widget ) )
		{
			return;
		}
		if ( e.widget instanceof Button
				&& ( ( (Button) e.widget ).getStyle( ) & SWT.TOGGLE ) == SWT.TOGGLE
				&& ( (Button) e.widget ).getSelection( ) )
		{
			selectAllButtons( false );
			( (Button) e.widget ).setSelection( true );
		}

		if ( e.widget.equals( btnAxisTitle ) )
		{
			popupShell = createPopupShell( );
			popupSheet = new AxisTextSheet( popupShell,
					getChart( ),
					getAxisForProcessing( ),
					AngleType.X );
			getWizard( ).attachPopup( btnAxisTitle.getText( ), -1, -1 );
		}
		else if ( e.widget.equals( btnGridlines ) )
		{
			popupShell = createPopupShell( );
			popupSheet = new AxisGridLinesSheet( popupShell,
					getChart( ),
					getAxisForProcessing( ) );
			getWizard( ).attachPopup( btnGridlines.getText( ), -1, -1 );
		}
		else if ( e.widget.equals( btnMarkers ) )
		{
			popupShell = createPopupShell( );
			popupSheet = new AxisMarkersSheet( popupShell,
					getChart( ),
					getAxisForProcessing( ) );
			getWizard( ).attachPopup( btnMarkers.getText( ), -1, -1 );
		}
		else if ( e.widget.equals( btnMoreOpt ) )
		{
			popupShell = createPopupShell( );
			popupSheet = new MoreOptionsAxisXSheet( popupShell,
					getChart( ),
					getAxisForProcessing( ) );
			getWizard( ).attachPopup( btnMoreOpt.getText( ), -1, -1 );
		}

		if ( e.widget.equals( btnVisible ) )
		{
			getAxisForProcessing( ).getLabel( )
					.setVisible( btnVisible.getSelection( ) );
			refreshPopupSheet( );
		}
	}

	public void widgetDefaultSelected( SelectionEvent e )
	{
		// TODO Auto-generated method stub
	}

	private Axis getAxisForProcessing( )
	{
		return ChartUIUtil.getAxisXForProcessing( (ChartWithAxes) getChart( ) );
	}

	protected void selectAllButtons( boolean isSelected )
	{
		btnAxisTitle.setSelection( isSelected );
		btnGridlines.setSelection( isSelected );
		btnMarkers.setSelection( isSelected );
		btnMoreOpt.setSelection( isSelected );
	}

}