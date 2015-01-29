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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main application entry point.
 */
public class Main {

	private final String APPLICATON_HEADER = "NAND Byte Plotter";

	/**
	 * Application entry point.
	 * 
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		new Main().run(args);
	}

	/**
	 * Main run point.
	 * 
	 * @param args The command line arguments.
	 */
	@SuppressWarnings("static-access")
	public void run(String[] args) {
		System.out.println(APPLICATON_HEADER);

		// Parse the CLI to find out if we are attempting to identify the relocations, or just plotting the NAND.
		CommandLineParser parser = new BasicParser();
		Options options = new Options();
		//options.addOption("l", "locate-relocations", false, "Attempt to location relocations the NAND");
		options.addOption("p", "byteplot", false, "Byte Plot the NAND file (default operation)");
		options.addOption("?", "help", false, "print this message");
		options.addOption("v", "version", false, "version information");
		options.addOption(OptionBuilder.withLongOpt("file").withDescription("NAND raw image file").withArgName("FILE")
				.hasArg().create("f"));
		options.addOption("l", "spare-location", true, "The location of spare: inband or end (default)");
		options.addOption(OptionBuilder.withLongOpt("userdata-size").withDescription("use SIZE-byte blocks").hasArg()
				.withArgName("SIZE").create());
		options.addOption(OptionBuilder.withLongOpt("spare-size").withDescription("use SIZE-byte spare blocks")
				.hasArg().withArgName("SIZE").create());
		options.addOption(OptionBuilder.withLongOpt("tile-size").withDescription("use SIZE-byte tile").hasArg()
				.withArgName("SIZE").create());

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			// Mandatory
			String filename = getString(line, "file");
			int userDataSize = getInteger(line, "userdata-size");
			int spareSize = getInteger(line, "spare-size");

			// Optional
			boolean spareNotInband = true;
			if (line.hasOption("spare-location")) {
				String val = line.getOptionValue("spare-location");
				if (val.equals("inband")) {
					spareNotInband = false;
				} else if (val.equals("end")) {
					spareNotInband = true;
				} else {
					HelpFormatter formatter = new HelpFormatter();
					formatter.printHelp("byteplot", options);
					System.exit(0);
				}
			}
			
			boolean plot = true;
//			if (line.hasOption("l")) {
//				plot = false;
//			}

			// Help and Version.
			if (line.hasOption("v")) {
				System.out.println("Version 1.0.0.0");
			}
			if (line.hasOption("?")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("byteplot", options);
				System.exit(0);
			}

			if (plot) {
				int tile = getInteger(line, "tile-size");
				new NANDBytePlot(filename, userDataSize, spareSize, tile, spareNotInband);
			} else {
				new IdentifyRelocations(filename, userDataSize, spareSize, spareNotInband);
			}

		} catch (ParseException  exp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("byteplot", options);
			System.exit(0);
		} catch (NumberFormatException exp) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("byteplot", options);
			System.exit(0);
		}
	}

	private String getString(CommandLine line, String parameter) throws ParseException {
		if (line.hasOption(parameter)) {
			return line.getOptionValue(parameter);
		} else {
			throw new ParseException(parameter);
		}
	}

	private int getInteger(CommandLine line, String parameter) throws ParseException {
		if (line.hasOption(parameter)) {
			try {
				return Integer.parseInt(line.getOptionValue(parameter));
			} catch (NumberFormatException e) {
				throw new ParseException(parameter);
			}
		} else {
			throw new ParseException(parameter);
		}
	}

	private boolean getBoolean(CommandLine line, String parameter) throws ParseException {
		if (line.hasOption(parameter)) {
			return Boolean.parseBoolean(line.getOptionValue(parameter));
		} else {
			throw new ParseException(parameter);
		}
	}
}
