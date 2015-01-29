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
 * A nand device assumes that that underlying stream contains an image which is laid out page page spare spare...
 */
public class NandOOBandDevice implements NANDDevice {

	private final FileChannel bs;
	private final int pageSize;
	private final int spareSize;
	private final int blockSize;
	private final long oobOffset;
	private final long endPage;

	/**
	 * Create a NAND device.
	 * 
	 * @param pageSize The page size
	 * @param spareSize The spare size
	 * @param blockSize The block size
	 * @param stream The stream to read from.
	 * @throws IOException
	 */
	public NandOOBandDevice(int pageSize, int spareSize, int blockSize, RandomAccessFile stream) throws IOException {
		this.pageSize = pageSize;
		this.spareSize = spareSize;
		bs = stream.getChannel();
		this.blockSize = blockSize;
		endPage = stream.length() / (pageSize + spareSize);
		oobOffset = endPage * pageSize;
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
			pos = chunk * (pageSize);
			bs.position(pos);
			int read = bs.read(data);
			if (read != pageSize)
				throw new IOException("Page read returned wrong size page");
		}
		if (spare != null) {
			if (spare.remaining() != spareSize) {
				throw new IllegalArgumentException();
			}
			pos = oobOffset + chunk * spareSize;
			bs.position(pos);
			int read = bs.read(spare);
			if (read != spareSize)
				throw new IOException("Spare read returned wrong size spare");
		}
	}
}
