/*
 	NAND Byteplotter
    Copyright (C) 2014  Dr Bradley L Schatz

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/ 
package com.schatzforensic.views;

import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedOp;

public class PartitionedBytePlot extends BytePlot {

	/**
	 * The size of the blocks to use.
	 */
	private final int blockSize;
	/**
	 * The size of the spare
	 */
	private final int spareSize;
	/**
	 * The size of the chunks to write.
	 */
	private final int chunkSize;
	/**
	 * The size of the tiles.
	 */
	private final int tiles;
	/**
	 * The direction in which to draw the chunks.
	 */
	private final LayoutDirection direction;

	/**
	 * The direction of the writes.
	 */
	public enum LayoutDirection {
		HORIZONTAL, VERTICAL
	};

	/**
	 * Create a new partitioned Byte Plot.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param blockSize The block size to utilise.
	 * @param spareSize The size of the spare
	 * @param layoutDirection The direction of the layout.
	 */
	public PartitionedBytePlot(int width, int height, int blockSize, int spareSize, LayoutDirection layoutDirection) {
		super(width, height);
		this.blockSize = blockSize;
		this.spareSize = spareSize;
		this.chunkSize = blockSize + spareSize;
		this.tiles = width / chunkSize;
		this.width = (chunkSize + 2) * tiles;
		this.direction = layoutDirection;
		init();
	}

	/*
	 * (non-Javadoc)
	 * @see com.evimetry.views.BytePlot#addBuf(int, java.nio.ByteBuffer)
	 */
	@Override
	public void addBuf(int offset, ByteBuffer buf) {
		if (direction == LayoutDirection.HORIZONTAL) {
			addBufHorizontally(offset, buf);
		} else {
			addBufVertically(offset, buf);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.evimetry.views.BytePlot#render(java.io.OutputStream)
	 */
	@Override
	public void render(OutputStream os) throws IOException {
		double[][] matrix = { { 1.0D, 0.0D }, { 1.0D, 0.0D }, { 1.0D, 0.0D } };

		//double[] red = { 1.0D, 0D, 0D };

		ParameterBlock pb = new ParameterBlock();
		pb.addSource(im);
		pb.add(matrix);

		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

		ColorModel cm = RasterFactory.createComponentColorModel(DataBuffer.TYPE_BYTE, cs, false, false,
				Transparency.OPAQUE);

		SampleModel sm = cm.createCompatibleSampleModel(im.getWidth(), im.getHeight());

		ImageLayout imageLayout = new ImageLayout();
		imageLayout.setSampleModel(sm);
		imageLayout.setColorModel(cm);

		RenderingHints renderingHints = new RenderingHints(null);
		renderingHints.clear();
		renderingHints.put(JAI.KEY_IMAGE_LAYOUT, imageLayout);

		// Perform the band combine operation.

		RenderedOp ro = (RenderedOp) RenderedOp.wrapRenderedImage((RenderedImage) JAI.create("bandcombine", pb,
				renderingHints));
		BufferedImage bi = ro.getAsBufferedImage();

		for (int j = 0; j < width / (chunkSize + 2); j++) {
			int spareStartOffset = (j * (chunkSize + 2)) + blockSize;
			int endSpareOffset = (j * (chunkSize + 2)) + blockSize + 1 + spareSize;

			for (int yy = 0; yy < height; yy++) {
				bi.setRGB(spareStartOffset, yy, Color.red.getRGB());
				bi.setRGB(endSpareOffset, yy, Color.red.getRGB());
			}
		}
		ImageIO.write(bi, "png", os);
	}
	
	private void addBufHorizontally(int offset, ByteBuffer buf) {
		int y = offset / chunkSize;
		int index = 0;
		for (int j = 0; j < tiles; j++) {
			for (int i = 0; i < blockSize; i++) {
				byte val = buf.get(index);
				int x = j * (chunkSize + 2) + i;
				wr.setSample(x, y, 0, val);
				index++;
			}

			for (int i = 0; i < spareSize; i++) {
				byte val = buf.get(index);
				int x = j * (chunkSize + 2) + blockSize + 1 + i;
				wr.setSample(x, y, 0, val);
				index++;
			}
		}
	}

	private void addBufVertically(int offset, ByteBuffer buf) {
		int chunkNo = offset / chunkSize;
		int startx = chunkNo / height;
		int starty = chunkNo % height;
		int index = 0;

		while (index < buf.limit()) {
			for (int i = 0; i < blockSize; i++) {
				byte val = buf.get(index);
				int x = startx * (chunkSize + 2) + i;
				wr.setSample(x, starty, 0, val);
				index++;
			}

			for (int i = 0; i < spareSize; i++) {
				byte val = buf.get(index);

				int x = startx * (chunkSize + 2) + blockSize + 1 + i;
				wr.setSample(x, starty, 0, val);
				index++;
			}

			starty++;
			if (starty == height) {
				starty = 0;
				startx++;
			}
		}
	}

}
