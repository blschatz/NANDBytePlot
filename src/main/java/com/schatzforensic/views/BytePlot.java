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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

/**
 * BytePlot
 */
public class BytePlot {

	/**
	 * The Width of the byte plot in pixels.
	 */
	protected int width;
	/**
	 * The height of the byte plot in pixels.
	 */
	protected int height;
	/**
	 * The buffered image.
	 */
	protected BufferedImage im;
	/**
	 * The rasterisation engine.
	 */
	protected WritableRaster wr;

	/**
	 * Create a new BytePlot with given width and height
	 * 
	 * @param width The width of the byte plot in pixels.
	 * @param height The height of the byte plot in pixels.
	 */
	public BytePlot(int width, int height) {
		this.height = height;
		this.width = width;
	}

	/**
	 * Initialise the buffered image instance.
	 */
	protected void init() {
		im = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		wr = im.getRaster();
	}

	/**
	 * Add a buffer of data to the given offset.
	 * 
	 * @param offset The offset of the buffer in the whole image.
	 * @param buf The buffer.
	 */
	public void addBuf(int offset, ByteBuffer buf) {
		for (int i = 0; i < width; i++) {
			byte val = buf.get();
			int y = offset / width;
			wr.setSample(i, y, 0, val);
		}
	}

	/**
	 * Add a buffer of data to the given offset.
	 * 
	 * @param offset The offset of the buffer in the whole image.
	 * @param buf The buffer.
	 */
	public void addBuf(int offset, byte[] buf) {
		for (int i = 0; i < width; i++) {
			byte val = buf[i];
			int y = offset / width;
			wr.setSample(i, y, 0, val);
		}
	}

	/**
	 * Render the image to an output stream
	 * 
	 * @param os The output stream to render the image to.
	 * @throws IOException
	 */
	public void render(OutputStream os) throws IOException {
		ImageIO.write(im, "png", os);
	}
}
