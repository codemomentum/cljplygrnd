package org.codemomentum.cljplygrnd.fourth;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;

import java.util.Map;

public class JoinTopology {

    public static class DataSource1 extends BaseRichSpout {
        SpoutOutputCollector _collector;

        String[] data = new String[]{
                "alice @alice",
                "jack @jack",
                "daniels @daniels",};

        int index = 0;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
        }

        @Override
        public void nextTuple() {
            Utils.sleep(1000);
            String s = data[index];
            String[] split = s.split(" ");
            _collector.emit(new Values(split[0],split[1]));
            index++;
            if (index < 2) {
                index = 0;
            }
        }

        @Override
        public void ack(Object id) {
        }

        @Override
        public void fail(Object id) {
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("id","twitter"));
        }

    }

    public static class DataSource2 extends BaseRichSpout {
        SpoutOutputCollector _collector;

        String[] data = new String[]{
                "alice LONDON",
                "jack AMSTERDAM",
                "daniels LISBON",};

        int index = 0;

        @Override
        public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
            _collector = collector;
        }

        @Override
        public void nextTuple() {
            Utils.sleep(1000);
            String s = data[index];
            String[] split = s.split(" ");
            _collector.emit(new Values(split[0],split[1]));
            index++;
            if (index < 2) {
                index = 0;
            }
        }

        @Override
        public void ack(Object id) {
        }

        @Override
        public void fail(Object id) {
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("id","city"));
        }

    }

    public static class JoinerBolt extends BaseRichBolt {

        Map map;
        TopologyContext topologyContext;
        OutputCollector outputCollector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.map = map;
            this.topologyContext = topologyContext;
            this.outputCollector = outputCollector;
        }

        @Override
        public void execute(Tuple tuple) {
            String[] words = tuple.getValue(0).toString().split(" ");
            for (String word : words) {
                outputCollector.emit(new Values(word));
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("word"));
        }
    }

    public static class PrinterBolt extends BaseBasicBolt {

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
            System.out.println("** "+tuple);
            collector.emit(new Values(tuple));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer ofd) {
        }

    }

    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("ds1", new DataSource1(), 1);
        builder.setSpout("ds2", new DataSource2(), 1);

        builder.setBolt("printer", new PrinterBolt(), 1)
                .shuffleGrouping("ds1")
                .shuffleGrouping("ds2");

        builder.setBolt("joiner",new JoinerBolt(),1)
                .fieldsGrouping("printer",new Fields("id"));

        Config conf = new Config();
        conf.setDebug(true);


        conf.setMaxTaskParallelism(3);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());

        Thread.sleep(10000);

        cluster.shutdown();
    }
}
