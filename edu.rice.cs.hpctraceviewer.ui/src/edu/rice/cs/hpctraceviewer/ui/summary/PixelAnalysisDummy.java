package edu.rice.cs.hpctraceviewer.ui.summary;

import org.eclipse.swt.graphics.ImageData;

import edu.rice.cs.hpctraceviewer.data.ColorTable;
import edu.rice.cs.hpctraceviewer.data.SpaceTimeDataController;

public class PixelAnalysisDummy implements IPixelAnalysis 
{
	@Override
	public void analysisInit(SpaceTimeDataController dataTraces, ColorTable colorTable) {}

	@Override
	public void analysisPixelInit(int x) {}

	@Override
	public void analysisPixelXY(ImageData detailData, int x, int y, int pixelValue) {}

	@Override
	public void analysisPixelFinal(int pixel) {}

	@Override
	public void analysisFinal(ImageData detailData) {}

}
