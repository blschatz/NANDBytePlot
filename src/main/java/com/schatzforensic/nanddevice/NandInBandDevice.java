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
package com.schatzforensic.nanddevice;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * A nand device assumes that that underlying stream contains an image which is laid out page spare page spare...
 */
public class NandInBandDevice implements NANDDevice {

	private final FileChannel bs;
	private final int pageSize;
	private final int spareSize;
	private final int blockSize;

	/**
	 * Create a NAND device.
	 * 
	 * @param pageSize The size of the page
	 * @param spareSize The spare size.
	 * @param blockSize The block size
	 * @param stream The file to read from
	 * @throws IOException
	 */
	public NandInBandDevice(int pageSize, int spareSize, int blockSize, RandomAccessFile stream)
			throws IOException {
		this.pageSize = pageSize;
		this.spareSize = spareSize;
		bs = stream.getChannel();
		this.blockSize = blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.evimetry.byteplotter.NANDDevice#getSize()
	 */
	@Override
	public long getSize() throws IOException {
		return bs.size() / (pageSize + spareSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.evimetry.byteplotter.NANDDevice#getBlockSize()
	 */
	@Override
	public int getBlockSize() {
		return blockSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.evimetry.byteplotter.NANDDevice#getSpareSize()
	 */
	@Override
	public int getSpareSize() {
		return spareSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.evimetry.byteplotter.NANDDevice#getPageDataSize()
	 */
	@Override
	public int getPageDataSize() {
		return pageSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.evimetry.byteplotter.NANDDevice#readPage(long, java.nio.ByteBuffer, java.nio.ByteBuffer)
	 */
	@Override
	public void readPage(long chunk, ByteBuffer data, ByteBuffer spare) throws IOException {

		long pos;
		if (data != null) {
			if (data.remaining() != pageSize) {
				throw new IllegalArgumentException();
			}
			pos = chunk * (pageSize + spareSize);
			bs.position(pos);
			bs.read(data);
		}

		if (spare != null) {
			if (spare.remaining() != spareSize) {
				throw new IllegalArgumentException();
			}
			pos = (chunk * (pageSize + spareSize)) + pageSize;
			bs.position(pos);
			bs.read(spare);
		}
	}
}
