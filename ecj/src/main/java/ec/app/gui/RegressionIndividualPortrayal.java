package ec.app.gui;

import ec.app.regression.Regression;
import ec.app.regression.RegressionData;
import ec.display.portrayal.IndividualPortrayal;
import ec.gp.GPIndividual;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.CharArrayWriter;
import java.io.IOException;

import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Log;
import ec.util.LogRestarter;
import ec.util.Parameter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RegressionIndividualPortrayal extends IndividualPortrayal
    {
        private static final LogRestarter restarter = new LogRestarter()
        {
            public Log reopen(Log l)
                    throws IOException
            {
                return null;
            }

            public Log restart(Log l)
                    throws IOException
            {
                return null;
            }
        };
        final JTextPane textPane;
        private CharArrayWriter printIndividualWriter;
        JFreeChart chart;
        private JSplitPane individualDisplayPane = null;

        public RegressionIndividualPortrayal() {
            super(new BorderLayout());

            seriesCollection = new XYSeriesCollection();
            seriesCollection.addSeries(new XYSeries("Actual"));
            seriesCollection.addSeries(new XYSeries("Expected"));
            textPane = new JTextPane();
            textPane.setEditable(false);
             chart = ChartFactory
                    .createXYLineChart(null,"x","f(x)",seriesCollection,
                            PlotOrientation.VERTICAL,true,false,false);
            individualDisplayPane = new JSplitPane();
            individualDisplayPane.setPreferredSize(new Dimension(100,100));
            individualDisplayPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            individualDisplayPane.setTopComponent(textPane);
            individualDisplayPane.setBottomComponent(new ChartPanel(chart));
            individualDisplayPane.setDividerLocation(0.2D);
            this.add(individualDisplayPane,BorderLayout.CENTER);

            printIndividualWriter = new CharArrayWriter();
        }

        public XYSeriesCollection seriesCollection;

        public void setup(EvolutionState state, Parameter base)
        {

        }

        @Override
        public void portrayIndividual(EvolutionState state, Individual individual) {
            int printIndividualLog = state.output.addLog(printIndividualWriter,restarter,false,false);

            individual.printIndividualForHumans(state,printIndividualLog);
            textPane.setText(printIndividualWriter.toString());
            textPane.setCaretPosition(0);
            state.output.removeLog(printIndividualLog);
            printIndividualWriter.reset();

            seriesCollection.getSeries(0).clear();
            Regression problem = (Regression) state.evaluator.p_problem.clone();
            for(int i = 0; i < problem.trainingSetSize; i++) {
                double x = problem.inputs[i];
                ((RegressionData)problem.input).x = x;
                problem.currentValue = x;
                ((GPIndividual)individual).trees[0].child.eval(
                        state,0,problem.input,problem.stack,((GPIndividual)individual),problem);
                double y = ((RegressionData) problem.input).x;
                seriesCollection.getSeries(0).add(i, y);
                seriesCollection.getSeries(1).add(i, problem.func(x));
            }
        }
    }
