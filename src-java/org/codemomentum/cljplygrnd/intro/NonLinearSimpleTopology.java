package org.codemomentum.cljplygrnd.intro;


import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.RebalanceOptions;
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
import backtype.storm.utils.TimeCacheMap;

import java.util.Map;
import java.util.Random;

public class NonLinearSimpleTopology {

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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            collector.emit(new Values(random.nextInt(50)));
        }
    }

    public static class DoubleAndTripleBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
        }

        @Override
        public void execute(Tuple input) {
            int val = input.getInteger(0);
            if (val % 2 == 0)
                collector.emit("even", input, new Values(val * 2, val * 3));
            else
                collector.emit("odd", input, new Values(val * 2, val * 3));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declareStream("even", new Fields("double", "triple"));
            declarer.declareStream("odd", new Fields("double", "triple"));
        }
    }

    public static class EvenBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
        }

        @Override
        public void execute(Tuple input) {
            collector.emit(new Values(input.getInteger(0)));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("even"));
        }
    }

    public static class OddBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
        }

        @Override
        public void execute(Tuple input) {
            collector.emit(new Values(input.getInteger(0)));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("odd"));
        }
    }

    public static class GlobalBolt extends BaseRichBolt {
        transient TimeCacheMap timeCacheMap;

        private OutputCollector collector;

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
            this.collector = outputCollector;
            this.timeCacheMap = new TimeCacheMap(30, 100);
        }

        @Override
        public void execute(Tuple input) {
            Object o = timeCacheMap.get(""+input.getInteger(0));
            if (null!=o) {
                System.out.println("OBJECT: "+ o +" was seen at most 30 seconds ago *******");
            }
            collector.emit(new Values(input));
            timeCacheMap.put(""+input.getInteger(0), input.getInteger(0));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("odd"));
        }

        @Override
        public void cleanup() {
            System.out.println("******* CLEANUP *******");
        }
    }

    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("RandomNumberSpout", new RandomNumberSpout(), 1);

        builder.setBolt("DoubleAndTripleBolt", new DoubleAndTripleBolt(), 1)
                .shuffleGrouping("RandomNumberSpout");

        builder.setBolt("EvenBolt", new EvenBolt(), 1)
                .shuffleGrouping("DoubleAndTripleBolt", "even");

        builder.setBolt("OddBolt", new OddBolt(), 1)
                .shuffleGrouping("DoubleAndTripleBolt", "odd");

        builder.setBolt("GlobalBolt", new GlobalBolt(), 1)
                .shuffleGrouping("EvenBolt").shuffleGrouping("OddBolt");

        Config conf = new Config();
        conf.setDebug(true);

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());

        Thread.sleep(1000);

        RebalanceOptions rebalanceOptions = new RebalanceOptions();
        rebalanceOptions.set_num_workers(5);
        cluster.rebalance("test", rebalanceOptions);

        Thread.sleep(10000);
        cluster.shutdown();
    }
}
