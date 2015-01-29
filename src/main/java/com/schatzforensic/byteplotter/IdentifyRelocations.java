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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import com.schatzforensic.nanddevice.NANDDevice;
import com.schatzforensic.nanddevice.NandInBandDevice;
import com.schatzforensic.nanddevice.NandOOBandDevice;

public class IdentifyRelocations {

	public IdentifyRelocations(String file, int userDataSize, int spareSize, boolean OOBEDevice) {
		ByteBuffer headerLine = null;
		try {
			File source = Paths.get(file).toFile();
			RandomAccessFile is = new RandomAccessFile(source, "r");
			
			NANDDevice fis = null;
			if(OOBEDevice){
				fis = new NandOOBandDevice(userDataSize, spareSize, 64, is);
			} else {
				fis = new NandInBandDevice(userDataSize, spareSize, 64, is);
			}
			int chunkSize = (userDataSize + spareSize);
			long countChunks = is.length() / chunkSize;

			// paramaters match size of file
			if (source.length() % chunkSize != 0) {
				System.err.println("Warning: size of file isnt a multiple of page size");
			}

			ByteBuffer buf = ByteBuffer.allocate(chunkSize);
			buf.limit(userDataSize);
			ByteBuffer page = buf.slice();
			buf.limit(chunkSize);
			buf.position(userDataSize);
			ByteBuffer spare = buf.slice();

			String magic = "http://schatzforensic.com/2048-grid-2048-label.raw";
			byte[] magicbytes = magic.getBytes(Charset.forName("UTF-8"));

			for (long j = 0; j < countChunks; j++) {

				buf.clear();
				page.clear();
				spare.clear();
				fis.readPage(j, page, spare);

				if (headerLine != null) {
					analyse(headerLine, buf);
					System.exit(0);
				}
				if (matches(page, magicbytes)) {
					for (int f = 0; f < magicbytes.length; f++) {
						buf.put(f, (byte) 0);
					}
					buf.clear();
					headerLine = buf;
					buf = ByteBuffer.allocate(chunkSize);
					buf.limit(userDataSize);
					page = buf.slice();
					buf.limit(chunkSize);
					buf.position(userDataSize);
					spare = buf.slice();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void analyse(ByteBuffer headerLine, ByteBuffer buf) {
		StringBuffer translatedRuler = new StringBuffer();
		int last = -1;
		boolean firstrun = true;
		int offset = 0;
		int lastRulerOffset = 0;
		do {
			int u = headerLine.get();
			int l = (0xff & buf.get());
			int rulerOffset = (u << 8) + l;

			if (firstrun) {
				firstrun = false;
				last = rulerOffset;
				translatedRuler.append(String.format("Offset %d: ", offset));
				translatedRuler.append(String.format("%d", rulerOffset));
				lastRulerOffset = rulerOffset;
			} else {
				if (rulerOffset != last + 1) {
					translatedRuler.append(String.format("-%d (%d) \r\nOffset %d: %d ", last,
							(last - lastRulerOffset + 1), offset, rulerOffset));
					lastRulerOffset = rulerOffset;
				}
				last = rulerOffset;
			}
			offset++;

		} while (headerLine.remaining() > 0);

		System.out.println(translatedRuler.toString());

	}

	private boolean matches(ByteBuffer page, byte[] magicbytes) {
		byte[] pageBytes = page.array();
		for (int i = page.arrayOffset(); i < magicbytes.length; i++) {
			if (magicbytes[i] != pageBytes[page.arrayOffset() + i])
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		new IdentifyRelocations(args[0], Short.parseShort(args[1]), Short.parseShort(args[2]), true);
	}

}
