package org.codemomentum.cljplygrnd.fourth;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.generated.StormTopology;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.trident.TridentTopology;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.Filter;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.operation.builtin.Sum;
import storm.trident.spout.IBatchSpout;
import storm.trident.tuple.TridentTuple;

import java.util.Map;
import java.util.Random;


public class TridentTopologyLauncher {

    public static class RandomNumberBatchSpout implements IBatchSpout {
        Random random;

        @Override
        public void open(Map map, TopologyContext topologyContext) {
            this.random = new Random();
        }

        @Override
        public void emitBatch(long batchSize, TridentCollector tridentCollector) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < batchSize; i++) {
                Values next = getNext();
                System.out.println("** Generated : " + next);
                tridentCollector.emit(next);
            }
        }

        private Values getNext() {
            return new Values(random.nextInt(100));
        }

        @Override
        public void ack(long l) {

        }

        @Override
        public void close() {

        }

        @Override
        public Map getComponentConfiguration() {
            return new Config();
        }

        @Override
        public Fields getOutputFields() {
            return new Fields("number");
        }
    }

    public static class TentimesFunction extends BaseFunction {
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            Integer number = tuple.getInteger(0);
            collector.emit(new Values(number * 10));
            System.out.println("** Processed " + (number * 10));
        }
    }

    public static class PrintFilter implements Filter {

        @Override
        public void prepare(Map conf, TridentOperationContext context) {
        }

        @Override
        public void cleanup() {
        }

        @Override
        public boolean isKeep(TridentTuple tuple) {
            System.out.println("***" + tuple);
            return true;
        }
    }

    public static StormTopology buildTopology() {
        TridentTopology topology = new TridentTopology();
        topology.newStream("RandomNumberSpout", new RandomNumberBatchSpout())
                .parallelismHint(16)
                .each(new Fields("number"), new TentimesFunction(), new Fields("double"))
                .each(new Fields("double"), new PrintFilter());
        return topology.build();
    }


    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.setMaxSpoutPending(20);
        conf.setNumWorkers(1);

        LocalDRPC drpc = new LocalDRPC();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("numbertopology", conf, buildTopology());

    }
}
