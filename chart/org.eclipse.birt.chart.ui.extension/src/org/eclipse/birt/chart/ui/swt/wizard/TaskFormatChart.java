/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.chart.ui.swt.wizard;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.DialChart;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.ui.extension.i18n.Messages;
import org.eclipse.birt.chart.ui.swt.interfaces.IRegisteredSheetEntry;
import org.eclipse.birt.chart.ui.swt.interfaces.IRegisteredSubtaskEntry;
import org.eclipse.birt.chart.ui.swt.interfaces.ITaskChangeListener;
import org.eclipse.birt.chart.ui.swt.interfaces.IUIManager;
import org.eclipse.birt.chart.ui.swt.wizard.internal.ChartPreviewPainter;
import org.eclipse.birt.core.ui.frameworks.taskwizard.interfaces.ISubtaskSheet;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */

public class TaskFormatChart extends TreeCompoundTask
		implements
			IUIManager,
			ITaskChangeListener
{

	private transient ChartPreviewPainter previewPainter = null;

	private transient Composite cmpTask;

	private transient Composite cmpAttribute;

	private transient Canvas previewCanvas;

	private transient Color whiteColor;

	private transient int iBaseSeriesCount = 0;

	private transient int iOrthogonalSeriesCount = 0;

	private transient int iBaseAxisCount = 0;

	private transient int iOrthogonalAxisCount = 0;

	private transient int iAncillaryAxisCount = 0;

	private static final String BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES = "BaseSeriesSheetsCWA"; //$NON-NLS-1$

	private static final String ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES = "OrthogonalSeriesSheetsCWA"; //$NON-NLS-1$

	private static final String BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES = "BaseSeriesSheetsCWOA"; //$NON-NLS-1$

	private static final String ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES = "OrthogonalSeriesSheetsCWOA"; //$NON-NLS-1$

	private static final String BASE_AXIS_SHEET_COLLECTION = "BaseAxisSheets"; //$NON-NLS-1$

	private static final String ORTHOGONAL_AXIS_SHEET_COLLECTION = "OrthogonalAxisSheets"; //$NON-NLS-1$

	private static final String ANCILLARY_AXIS_SHEET_COLLECTION = "AncillaryAxisSheets"; //$NON-NLS-1$

	private static final String[] BASE_SERIES_SHEETS_FOR_CHARTS_WITH_AXES = new String[]{
		"Series.X Series"}; //$NON-NLS-1$ 

	private static final String[] ORTHOGONAL_SERIES_SHEETS_FOR_CHARTS_WITH_AXES = new String[]{
		"Series.Y Series"}; //$NON-NLS-1$

	private static final String[] BASE_AXIS_SHEETS = new String[]{
			"Axis", "Axis.X Axis"}; //$NON-NLS-1$ //$NON-NLS-2$

	private static final String[] ORTHOGONAL_AXIS_SHEETS = new String[]{
		"Axis.Y Axis"};//$NON-NLS-1$

	private static final String[] ANCILLARY_AXIS_SHEETS = new String[]{
		"Axis.Z Axis"};//$NON-NLS-1$

	private static final String[] BASE_SERIES_SHEETS_FOR_CHARTS_WITHOUT_AXES = new String[]{
		"Series.Base Series"}; //$NON-NLS-1$ 

	private static final String[] ORTHOGONAL_SERIES_SHEETS_FOR_CHARTS_WITHOUT_AXES = new String[]{
		"Series.Orthogonal Series"}; //$NON-NLS-1$

	public TaskFormatChart( )
	{
		super( Messages.getString( "TaskFormatChart.TaskExp" ) ); //$NON-NLS-1$
	}

	protected void populateSubtasks( )
	{
		super.populateSubtasks( );

		// Get collection of registered Sheets
		Collection cRegisteredEntries = ChartUIExtensionsImpl.instance( )
				.getUISheetExtensions( );
		Iterator iterEntries = cRegisteredEntries.iterator( );

		// Vector to be used to build a sorted list of registered sheets (sorted
		// on provided node index)
		Vector vSortedEntries = new Vector( );
		while ( iterEntries.hasNext( ) )
		{
			IRegisteredSubtaskEntry entry = (IRegisteredSubtaskEntry) iterEntries.next( );
			if ( vSortedEntries.isEmpty( ) )
			{
				vSortedEntries.add( entry );
			}
			else
			{
				// Find location where the new entry needs to be inserted
				int iNewIndex = entry.getNodeIndex( );

				// Try the last entry
				if ( ( (IRegisteredSubtaskEntry) vSortedEntries.get( vSortedEntries.size( ) - 1 ) ).getNodeIndex( ) <= iNewIndex )
				{
					vSortedEntries.add( entry );
				}
				else if ( ( (IRegisteredSubtaskEntry) vSortedEntries.get( 0 ) ).getNodeIndex( ) > iNewIndex )
				// Try the first entry
				{
					vSortedEntries.add( 0, entry );
				}
				else
				{
					vSortedEntries = addEntrySorted( vSortedEntries,
							entry,
							0,
							vSortedEntries.size( ) - 1 );
				}
			}
		}

		for ( int i = 0; i < vSortedEntries.size( ); i++ )
		{
			IRegisteredSubtaskEntry entry = (IRegisteredSubtaskEntry) vSortedEntries.get( i );
			ISubtaskSheet sheet = entry.getSheet( );
			String sNodePath = entry.getNodePath( );

			// Initially ALL registered sheets are visible
			htVisibleSheets.put( sNodePath, sheet );

			addSubtask( sNodePath, sheet );
		}

		if ( getCurrentModelState( ) != null )
		{
			initialize( getCurrentModelState( ), this );
		}
	}

	protected void updateTreeItem( )
	{
		getNavigatorTree( ).removeAll( );
		Iterator itKeys = htVisibleSheets.keySet( ).iterator( );
		while ( itKeys.hasNext( ) )
		{
			String sKey = (String) itKeys.next( );
			Object oVal = htVisibleSheets.get( sKey );
			if ( oVal instanceof Vector )
			{
				for ( int i = 0; i < ( (Vector) oVal ).size( ); i++ )
				{
					String sSuffix = ""; //$NON-NLS-1$
					if ( ( (Vector) oVal ).size( ) > 1 )
					{
						sSuffix = INDEX_SEPARATOR + String.valueOf( i + 1 );
					}
					// If parent is dynamic as well
					String sParentKey = sKey.substring( 0,
							sKey.lastIndexOf( "." ) ); //$NON-NLS-1$
					Object oParentVal = htVisibleSheets.get( sParentKey );
					if ( oParentVal != null && oParentVal instanceof Vector )
					{
						getNavigatorTree( ).addNode( sParentKey
								+ sSuffix
								+ "." + sKey.substring( sKey.lastIndexOf( "." ) ) + sSuffix ); //$NON-NLS-1$ //$NON-NLS-2$
					}
					else
					{
						getNavigatorTree( ).addNode( sKey + sSuffix );
					}
				}
			}
			else
			{
				getNavigatorTree( ).addNode( sKey );
			}
		}
	}

	private Vector addEntrySorted( Vector vSortedEntries,
			IRegisteredSubtaskEntry entry, int iStart, int iEnd )
	{
		int iNewIndex = entry.getNodeIndex( );
		if ( iStart == iEnd )
		{
			if ( ( (IRegisteredSheetEntry) vSortedEntries.get( iStart ) ).getNodeIndex( ) > iNewIndex )
			{
				vSortedEntries.add( iStart, entry );
			}
			else
			{
				vSortedEntries.add( iEnd + 1, entry );
			}
		}
		else if ( ( iEnd - iStart ) == 1 )
		{
			vSortedEntries.add( iEnd, entry );
		}
		else
		{
			if ( ( (IRegisteredSubtaskEntry) vSortedEntries.get( iStart ) ).getNodeIndex( ) == iNewIndex )
			{
				vSortedEntries.add( iStart + 1, entry );
			}
			else
			{
				int iHalfwayPoint = ( iEnd - iStart ) / 2;
				if ( ( (IRegisteredSubtaskEntry) vSortedEntries.get( iStart
						+ iHalfwayPoint ) ).getNodeIndex( ) > iNewIndex )
				{
					addEntrySorted( vSortedEntries,
							entry,
							iStart,
							( iStart + iHalfwayPoint ) );
				}
				else
				{
					addEntrySorted( vSortedEntries,
							entry,
							( iStart + iHalfwayPoint ),
							iEnd );
				}
			}
		}
		return vSortedEntries;
	}

	public boolean registerSheetCollection( String sCollection,
			String[] saNodePaths )
	{
		try
		{
			htSheetCollections.put( sCollection, saNodePaths );
			return true;
		}
		catch ( Throwable e )
		{
			return false;
		}
	}

	public String[] getRegisteredCollectionValue( String sCollection )
	{
		Object oArr = htSheetCollections.get( sCollection );
		if ( oArr == null )
		{
			return null;
		}
		return (String[]) oArr;
	}

	public boolean addCollectionInstance( String sCollection )
	{
		if ( !htSheetCollections.containsKey( sCollection ) )
		{
			return false;
		}
		String[] saNodes = (String[]) htSheetCollections.get( sCollection );
		for ( int iN = 0; iN < saNodes.length; iN++ )
		{
			addVisibleSubtask( saNodes[iN] );
		}
		return true;
	}

	private void addVisibleSubtask( String sNodeName )
	{
		Vector vSheets = new Vector( );
		// check if node exists in tree
		if ( htVisibleSheets.containsKey( sNodeName ) )
		{
			Object oSheets = htVisibleSheets.get( sNodeName );
			if ( oSheets instanceof Vector )
			{
				vSheets = (Vector) oSheets;
			}
			else if ( oSheets instanceof ISubtaskSheet )
			{
				vSheets.add( oSheets );
			}
			else
			{
				return;
			}
			vSheets.add( getSubtask( sNodeName ) );
			htVisibleSheets.put( sNodeName, vSheets );
		}
		else
		{
			if ( containSubtask( sNodeName ) )
			{
				vSheets.add( getSubtask( sNodeName ) );
				htVisibleSheets.put( sNodeName, vSheets );
			}
		}
	}

	private void removeVisibleTask( String sNodeName )
	{
		Vector vSheets = new Vector( );
		// check if node exists in tree
		if ( htVisibleSheets.containsKey( sNodeName ) )
		{
			Object oSheets = htVisibleSheets.get( sNodeName );
			if ( oSheets instanceof Vector )
			{
				vSheets = (Vector) oSheets;
			}
			else if ( oSheets instanceof ISubtaskSheet )
			{
				vSheets.add( oSheets );
			}
			else
			{
				return;
			}

			int iLast = vSheets.lastIndexOf( getSubtask( sNodeName ) );
			vSheets.remove( iLast );
			htVisibleSheets.put( sNodeName, vSheets );
		}
		else
		{
			if ( containSubtask( sNodeName ) )
			{
				int iLast = vSheets.lastIndexOf( getSubtask( sNodeName ) );
				vSheets.remove( iLast );
				htVisibleSheets.put( sNodeName, vSheets );
			}
		}
	}

	public boolean removeCollectionInstance( String sCollection )
	{
		if ( !htSheetCollections.containsKey( sCollection ) )
		{
			return false;
		}
		String[] saNodes = (String[]) htSheetCollections.get( sCollection );
		for ( int iN = 0; iN < saNodes.length; iN++ )
		{
			removeVisibleTask( saNodes[iN] );
		}
		return true;
	}

	public Chart getCurrentModelState( )
	{
		if ( getContext( ) == null )
		{
			return null;
		}
		return ( (ChartWizardContext) getContext( ) ).getModel( );
	}

	public Composite getUI( Composite parent )
	{
		Composite cmp = super.getUI( parent );
		createPreviewPainter( );
		return cmp;
	}

	protected Composite createContainer( Composite parent )
	{
		cmpTask = new Composite( parent, SWT.NONE );
		{
			cmpTask.setLayout( new GridLayout( ) );
			GridData gridData = new GridData( GridData.FILL_BOTH );
			cmpTask.setLayoutData( gridData );
		}
		createPreviewArea( );

		cmpAttribute = new Composite( cmpTask, SWT.NONE );
		{
			cmpAttribute.setLayout( new GridLayout( ) );
			GridData gridData = new GridData( );
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = SWT.FILL;
			gridData.verticalAlignment = SWT.FILL;
			cmpAttribute.setLayoutData( gridData );
		}

		return cmpTask;
	}

	private void createPreviewArea( )
	{
		Label label = new Label( cmpTask, SWT.NONE );
		{
			label.setFont( JFaceResources.getBannerFont( ) );
			label.setText( Messages.getString( "TaskFormatChart.Label.Preview" ) ); //$NON-NLS-1$
		}

		previewCanvas = new Canvas( cmpTask, SWT.NONE );
		{
			GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
			gridData.verticalAlignment = SWT.FILL;
			gridData.heightHint = 250;
			previewCanvas.setLayoutData( gridData );
			whiteColor = new Color( Display.getDefault( ), 255, 255, 255 );
			previewCanvas.setBackground( whiteColor );
		}
	}

	private void createPreviewPainter( )
	{
		if ( previewPainter == null )
		{
			previewPainter = new ChartPreviewPainter( ( (ChartWizardContext) getContext( ) ).getProcessor( ) );
			previewCanvas.addPaintListener( previewPainter.getPaintListener( ) );
			previewPainter.setPreview( previewCanvas );
		}
		previewPainter.renderModel( getCurrentModelState( ) );
	}

	public void changeTask( )
	{
		if ( cmpTask != null )
		{
			previewPainter.renderModel( getCurrentModelState( ) );
		}
	}

	private void initialize( Chart chartModel, IUIManager uiManager )
	{
		// Register sheet collections
		uiManager.registerSheetCollection( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES,
				BASE_SERIES_SHEETS_FOR_CHARTS_WITH_AXES );
		uiManager.registerSheetCollection( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES,
				ORTHOGONAL_SERIES_SHEETS_FOR_CHARTS_WITH_AXES );
		uiManager.registerSheetCollection( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES,
				BASE_SERIES_SHEETS_FOR_CHARTS_WITHOUT_AXES );
		uiManager.registerSheetCollection( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES,
				ORTHOGONAL_SERIES_SHEETS_FOR_CHARTS_WITHOUT_AXES );
		uiManager.registerSheetCollection( BASE_AXIS_SHEET_COLLECTION,
				BASE_AXIS_SHEETS );
		uiManager.registerSheetCollection( ORTHOGONAL_AXIS_SHEET_COLLECTION,
				ORTHOGONAL_AXIS_SHEETS );
		uiManager.registerSheetCollection( ANCILLARY_AXIS_SHEET_COLLECTION,
				ANCILLARY_AXIS_SHEETS );

		if ( chartModel instanceof ChartWithAxes )
		{
			iBaseAxisCount = ( (ChartWithAxes) chartModel ).getAxes( ).size( );
			iOrthogonalAxisCount = 0;
			iAncillaryAxisCount = 0;
			iBaseSeriesCount = 0;
			iOrthogonalSeriesCount = 0;
			for ( int i = 0; i < iBaseAxisCount; i++ )
			{
				iBaseSeriesCount += ( (Axis) ( (ChartWithAxes) chartModel ).getAxes( )
						.get( i ) ).getSeriesDefinitions( ).size( );
				iOrthogonalAxisCount += ( (Axis) ( (ChartWithAxes) chartModel ).getAxes( )
						.get( i ) ).getAssociatedAxes( ).size( );
				if ( chartModel.getDimension( ).getValue( ) == ChartDimension.THREE_DIMENSIONAL )
				{
					iAncillaryAxisCount += ( (Axis) ( (ChartWithAxes) chartModel ).getAxes( )
							.get( i ) ).getAncillaryAxes( ).size( );
				}
				for ( int iS = 0; iS < iOrthogonalAxisCount; iS++ )
				{
					iOrthogonalSeriesCount += ( (Axis) ( (Axis) ( (ChartWithAxes) chartModel ).getAxes( )
							.get( i ) ).getAssociatedAxes( ).get( iS ) ).getSeriesDefinitions( )
							.size( );
				}
			}
			// Start from 1 because there will always be at least 1 entry for
			// each registered sheet when this method is called
			for ( int iBA = 1; iBA < iBaseAxisCount; iBA++ )
			{
				uiManager.addCollectionInstance( BASE_AXIS_SHEET_COLLECTION );
			}
			for ( int iOA = 1; iOA < iOrthogonalAxisCount; iOA++ )
			{
				uiManager.addCollectionInstance( ORTHOGONAL_AXIS_SHEET_COLLECTION );
			}

			// Remove Z axis by default
			uiManager.removeCollectionInstance( ANCILLARY_AXIS_SHEET_COLLECTION );
			// Must start from 0 because default is 0
			for ( int iOA = 0; iOA < iAncillaryAxisCount; iOA++ )
			{
				uiManager.addCollectionInstance( ANCILLARY_AXIS_SHEET_COLLECTION );
			}
			// Remove series sheets (for charts with axes) since they are not
			// needed for Charts Without Axes
			uiManager.removeCollectionInstance( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES );
			uiManager.removeCollectionInstance( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES );

			for ( int iBS = 1; iBS < iBaseSeriesCount; iBS++ )
			{
				uiManager.addCollectionInstance( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES );
			}
			for ( int iOS = 1; iOS < iOrthogonalSeriesCount; iOS++ )
			{
				uiManager.addCollectionInstance( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES );
			}
		}
		else
		{
			iBaseAxisCount = 0;
			iOrthogonalAxisCount = 0;
			iBaseSeriesCount = ( (ChartWithoutAxes) chartModel ).getSeriesDefinitions( )
					.size( );
			iOrthogonalSeriesCount = 0;
			for ( int iS = 0; iS < iBaseSeriesCount; iS++ )
			{
				iOrthogonalSeriesCount += ( (SeriesDefinition) ( (ChartWithoutAxes) chartModel ).getSeriesDefinitions( )
						.get( iS ) ).getSeriesDefinitions( ).size( );
			}

			// Remove axis sheets since they are not needed for Charts Without
			// Axes
			uiManager.removeCollectionInstance( ANCILLARY_AXIS_SHEET_COLLECTION );
			uiManager.removeCollectionInstance( ORTHOGONAL_AXIS_SHEET_COLLECTION );
			uiManager.removeCollectionInstance( BASE_AXIS_SHEET_COLLECTION );
			// Remove series sheets (for charts with axes) since they are not
			// needed for Charts Without Axes
			uiManager.removeCollectionInstance( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES );
			uiManager.removeCollectionInstance( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITH_AXES );

			if ( chartModel instanceof DialChart )
			{
				uiManager.removeCollectionInstance( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES );
			}
			else
			{
				for ( int iBS = 1; iBS < iBaseSeriesCount; iBS++ )
				{
					uiManager.addCollectionInstance( BASE_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES );
				}
			}
			for ( int iOS = 1; iOS < iOrthogonalSeriesCount; iOS++ )
			{
				uiManager.addCollectionInstance( ORTHOGONAL_SERIES_SHEET_COLLECTION_FOR_CHARTS_WITHOUT_AXES );
			}
		}
	}

	public void widgetDisposed( DisposeEvent e )
	{
		super.widgetDisposed( e );
		cmpTask = null;
		cmpAttribute = null;
		previewCanvas = null;
		previewPainter = null;
		if ( whiteColor != null && !whiteColor.isDisposed( ) )
		{
			whiteColor.dispose( );
		}
		whiteColor = null;
	}
}
