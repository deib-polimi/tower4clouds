/**
 * Copyright (C) 2014 Politecnico di Milano (marco.miglierina@polimi.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.polimi.tower4clouds.rules.batch;

import it.polimi.modaclouds.qos_models.Problem;
import it.polimi.modaclouds.qos_models.QoSValidator;
import it.polimi.modaclouds.qos_models.schema.Constraints;
import it.polimi.modaclouds.qos_models.util.XMLHelper;
import it.polimi.tower4clouds.rules.MonitoringRuleFactory;
import it.polimi.tower4clouds.rules.MonitoringRules;
import it.polimi.tower4clouds.rules.RulesValidator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

public class BatchTool {

	public static void main(String[] args) {
		Options options = buildOptions();
		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		FileInputStream inputFile = null;
		try {
			// parse the command line arguments
			CommandLine cmd = parser.parse(options, args);
			if (cmd.getOptions().length != 1) {
				System.err
				.println("Parsing failed: Reason: one and only one option is required");
				formatter.printHelp("qos-models", options);
			} else if (cmd.hasOption("h")) {
				formatter.printHelp("qos-models", options);
			} else if (cmd.hasOption("v")) {
				String file = cmd.getOptionValue("v");
				inputFile = new FileInputStream(file);
				MonitoringRules rules = XMLHelper.deserialize(inputFile,
						MonitoringRules.class);
				RulesValidator validator = new RulesValidator();
				Set<Problem> problems = validator.validateAllRules(rules);
				printResult(problems);
			} else if (cmd.hasOption("c")) {
				String file = cmd.getOptionValue("c");
				inputFile = new FileInputStream(file);
				Constraints constraints = XMLHelper.deserialize(inputFile,
						Constraints.class);
				QoSValidator validator = new QoSValidator();
				Set<Problem> problems = validator
						.validateAllConstraints(constraints);
				printResult(problems);
			} else if (cmd.hasOption("r")) {
				String file = cmd.getOptionValue("r");
				inputFile = new FileInputStream(file);
				Constraints constraints = XMLHelper.deserialize(inputFile,
						Constraints.class);
				MonitoringRuleFactory factory = new MonitoringRuleFactory();
				MonitoringRules rules = factory
						.makeRulesFromQoSConstraints(constraints);
				XMLHelper.serialize(rules, System.out);
			}
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			formatter.printHelp("qos-models", options);
		} catch (FileNotFoundException e) {
			System.err
			.println("Could not locate input file: " + e.getMessage());
		} catch (JAXBException | SAXException e) {
			System.err.println("Input file could not be parsed: ");			
			e.printStackTrace();
		}
		catch (Exception e) {
			System.err
			.println("Unknown error: ");
			e.printStackTrace();
		} finally {
			if (inputFile != null) {
				try {
					inputFile.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static void printResult(Set<Problem> problems) {
		if (problems.isEmpty())
			System.out.println("Validation successful");
		else {
			System.out.println("Validation failed. Details follow:");
			for (Problem p : problems) {
				System.out.println("Monitoring rule " + p.getId() + ": "
						+ p.getError() + " at position " + p.getTagName()
						+ ", " + p.getDescription());
			}
		}
	}

	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Options options = new Options();
		options.addOption(OptionBuilder.withDescription("print this message")
				.withLongOpt("help").create("h"));
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("validate monitoring rules in the given file")
				.withLongOpt("validate-rules").create("v"));
		options.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("validate qos constraints in the given file")
				.withLongOpt("validate-constraints").create("c"));
		options.addOption(OptionBuilder
				.withArgName("file")
				.hasArg()
				.withLongOpt("make-rules")
				.withDescription(
						"make monitoring rules from qos constraints in the given file")
						.create("r"));
		return options;
	}
}
