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
import java.nio.ByteBuffer;

/**
 * Interface for NAND Devices.
 */
public interface NANDDevice {
	/**
	 * Read a page from the NAND Device.
	 * 
	 * @param chunk The chunk to read
	 * @param data The buffer for the data
	 * @param spare The buffer for the space
	 * @throws IOException If reading the information failed.
	 */
	public void readPage(long chunk, ByteBuffer data, ByteBuffer spare) throws IOException;

	/**
	 * Get the Page Data Size.
	 * 
	 * @return The page data size.
	 */
	public int getPageDataSize();

	/**
	 * Get the Spare Size.
	 * 
	 * @return The size of the spare.
	 */
	public int getSpareSize();

	/**
	 * Get the size of the NAND device in blocks.
	 * 
	 * @return The size of the NAND in blocks.
	 * @throws IOException
	 */
	public long getSize() throws IOException;

	/**
	 * Get the block size.
	 * 
	 * @return The size of the blocks.
	 */
	public int getBlockSize();

}
