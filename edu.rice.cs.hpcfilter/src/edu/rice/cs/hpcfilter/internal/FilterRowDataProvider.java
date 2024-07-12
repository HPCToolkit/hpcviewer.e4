// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpcfilter.internal;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class FilterRowDataProvider implements IDataProvider
{
	protected IDataProvider bodyDataProvider;
	
	public FilterRowDataProvider(IDataProvider bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}

	public void setDataProvider(IDataProvider bodyDataProvider) {
		this.bodyDataProvider = bodyDataProvider;
	}

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return this.bodyDataProvider.getRowCount();
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return Integer.valueOf(rowIndex + 1);
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }
}
