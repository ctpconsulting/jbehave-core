package org.jbehave.examples.trader;

import static java.util.Arrays.asList;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.AntClassLoader;
import org.jbehave.core.JUnitStory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.examples.trader.converters.TraderConverter;
import org.jbehave.examples.trader.model.Stock;
import org.jbehave.examples.trader.model.Trader;
import org.jbehave.examples.trader.persistence.TraderPersister;
import org.jbehave.examples.trader.service.TradingService;

/**
 * Example of how to run a story using a JBehave2 style inheritance. A story
 * just need to extend this abstract class and are out-of-the-box runnable via
 * JUnit.
 */
public abstract class TraderStory extends JUnitStory {

	public TraderStory() {

		// start with default story configuration, overriding story loader and reporter
        StoryPathResolver storyPathResolver = new UnderscoredCamelCaseResolver(".story");
        Class<? extends TraderStory> storyClass = this.getClass();
        Properties rendering = new Properties();
        rendering.put("decorateNonHtml", "true");
    	URL codeLocation = StoryLocation.codeLocationFromClass(storyClass);
		Configuration configuration = new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                	.withCodeLocation(codeLocation)
                	.withDefaultFormats()
                	.withRenderingResources(rendering)
                	.withFormats(CONSOLE, TXT, HTML, XML)
                	.withFailureTrace(false))
                .useStoryPathResolver(storyPathResolver)
                .useStepMonitor(new SilentStepMonitor())
                .useParameterConverters(new ParameterConverters(
        				new TraderConverter(mockTradePersister())))
        		.useStepPatternParser(new RegexPrefixCapturingPatternParser("%"));
        		
		useConfiguration(configuration);
		addSteps(createSteps(configuration));
		
	    configuredEmbedder().embedderControls().doIgnoreFailureInStories(true).doIgnoreFailureInReports(true);

	}

	protected List<CandidateSteps> createSteps(Configuration configuration) {
		return new InstanceStepsFactory(configuration, new TraderSteps(
				new TradingService()), new BeforeAfterSteps())
				.createCandidateSteps();
	}

	private TraderPersister mockTradePersister() {
		return new TraderPersister(new Trader("Mauro", asList(new Stock("STK1",
				10.d))));
	}

}
