package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MultiThreadedSumMatrix.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {
    private final int nthread;
    /**
     * 
     * @param nthread
     * number of thread performing the sum
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double[] array;
        private final int row;
        private double result;

        /**
         * 
         * @param array
         * array of sum
         * @param row
         * row of matrix
         */
        Worker(final double[] array, final int row) {
            super();
            this.array = array.clone();
            this.row = row;
        }

        @Override
        public void run() {
            System.out.println("Working from row " + row); //NOPMD
            for (final double num : array) {
                result += num;
            }
        }
        /**
         * Return the result of the sum.
         * @return
         * return the sum result
         */
        public double getResult() {
            return this.result;
        }
    }

    @Override
    public double sum(final double[][] matrix) {
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int i = 0; i < matrix.length; i++) {
            workers.add(new Worker(matrix[i], i));
        }
        workers.forEach(worker -> worker.start());

        double sum = 0;
        for (final Worker worker : workers) {
            try {
                worker.join();
                sum += worker.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }
}
