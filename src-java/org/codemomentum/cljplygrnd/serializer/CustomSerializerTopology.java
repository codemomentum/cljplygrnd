package org.codemomentum.cljplygrnd.serializer;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.Map;
import java.util.Random;

public class CustomSerializerTopology {

    public static class RandomNumberSpout extends BaseRichSpout {
        SpoutOutputCollector collector;
        Random random = new Random();

        @Override
        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("number"));
        }

        @Override
        public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
            this.collector = spoutOutputCollector;
        }

        @Override
        public void nextTuple() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            collector.emit(new Values(random.nextInt(100)));
        }
    }

    public static class CustomObjectWrapperBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
        }

        @Override
        public void execute(Tuple input) {
            int val = input.getInteger(0);
            collector.emit(input, new Values(new CustomBusinessObject(val)));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("custom"));
        }
    }

    public static class PrinterBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
        }

        @Override
        public void execute(Tuple input) {
            CustomBusinessObject customBusinessObject = (CustomBusinessObject) input.getValues().get(0);
            collector.emit(input, new Values(customBusinessObject.getNumber()));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("number"));
        }
    }


    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();


        builder.setSpout("RandomNumberSpout", new RandomNumberSpout(), 1);

        builder.setBolt("CustomObjectWrapperBolt", new CustomObjectWrapperBolt(), 1)
                .shuffleGrouping("RandomNumberSpout");

        builder.setBolt("PrinterBolt", new PrinterBolt(), 1).shuffleGrouping("CustomObjectWrapperBolt");

        Config conf = new Config();
        conf.registerSerialization(CustomBusinessObject.class, CustomBusinessObjectSerializer.class);
        conf.setFallBackOnJavaSerialization(false);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());

        Thread.sleep(10000);
        cluster.shutdown();
    }
}
