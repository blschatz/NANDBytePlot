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
package com.schatzforensic.byteplotter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.schatzforensic.nanddevice.NANDDevice;
import com.schatzforensic.nanddevice.NandInBandDevice;
import com.schatzforensic.nanddevice.NandOOBandDevice;
import com.schatzforensic.views.PartitionedBytePlot;

public class NANDBytePlot {

	public NANDBytePlot(String file, int userDataSize, int spareSize, int tiling, boolean spareNotInBand) {

		try {
			File source = new File(file);
			String basePath = source.getParent();

			RandomAccessFile is = new RandomAccessFile(source, "r");
			NANDDevice fis = null;
			if(spareNotInBand){
				fis = new NandOOBandDevice(userDataSize, spareSize, tiling, is);
			} else {
				fis = new NandInBandDevice(userDataSize, spareSize, tiling, is);
			}

			int imageMaxHeight = 4096 * 2;
			int pageSize = (userDataSize + spareSize);
			int width = pageSize * tiling;
			long countChunks = is.length() / pageSize;

			// paramaters match size of file
			if (source.length() % pageSize != 0) {
				System.err.println("Warning: size of file isnt a multiple of page size");
			}

			ByteBuffer buf = ByteBuffer.allocate(pageSize);
			buf.limit(userDataSize);
			ByteBuffer page = buf.slice();
			buf.limit(pageSize);
			buf.position(userDataSize);
			ByteBuffer spare = buf.slice();

			int chunksPerImage = (imageMaxHeight * tiling);
			int imagesToGenerate = (int) Math.ceil((double) countChunks / (double) chunksPerImage);

			int fileNo = 0;
			long read = 0;
			final long length = source.length();

			for (long j = 0; j < imagesToGenerate; j += 1) {
				int thisHeight = Math.min(imageMaxHeight, (int) (countChunks - (j * chunksPerImage)));
				PartitionedBytePlot v = new PartitionedBytePlot(width, (int) thisHeight, userDataSize, spareSize,
						PartitionedBytePlot.LayoutDirection.VERTICAL);

				for (int i = 0; i < thisHeight * tiling; i += 1) {
					if (length - read < pageSize) {
						break;
					}
					buf.clear();
					page.clear();
					spare.clear();
					fis.readPage(chunksPerImage * j + i, page, spare);
					v.addBuf((int) i * pageSize, buf);
					read += pageSize;
				}

				FileOutputStream os = new FileOutputStream(String.format("%s%s%s-%d-%d.%03d.png", basePath,
						File.separator, source.getName(), userDataSize, spareSize, fileNo));
				v.render(os);
				os.close();

				fileNo++;

				if (length - read < pageSize) {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new NANDBytePlot(args[0], Short.parseShort(args[1]), Short.parseShort(args[2]), Integer.parseInt(args[3]), true);
	}

}
