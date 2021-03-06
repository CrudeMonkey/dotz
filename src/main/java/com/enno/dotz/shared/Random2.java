package com.enno.dotz.shared;

import java.util.Random;

// see http://www.javamex.com/tutorials/random_numbers/java_util_random_subclassing.shtml
public class Random2 extends Random 
{   
    private long m_originalSeed;
    
    private static final long serialVersionUID = 3905348978240129619L;
    private static final long multiplier = 0x5deece66dL;
    /**
     * A value used to avoid two random number generators produced at the same
     * time having the same seed.
     */
    private static int uniqueSeed = 0;
    /**
     * The boolean value indicating if the second Gaussian number is available.
     * 
     * @serial
     */
    private boolean haveNextNextGaussian;
    /**
     * @serial It is associated with the internal state of this generator.
     */
    private long seed;
    /**
     * The second Gaussian generated number.
     * 
     * @serial
     */
    private double nextNextGaussian;
    
    /**
     * Construct a random generator with the current time of day in milliseconds
     * as the initial state.
     * 
     * @see #setSeed
     */
    public Random2() {
      setSeed(uniqueSeed++ + System.currentTimeMillis());
    }
    /**
     * Construct a random generator with the given {@code seed} as the initial
     * state.
     * 
     * @param seed the seed that will determine the initial state of this random
     *          number generator.
     * @see #setSeed
     */
    public Random2(long seed) {
      setSeed(seed);
    }
    /**
     * Returns the next pseudo-random, uniformly distributed {@code boolean} value
     * generated by this generator.
     * 
     * @return a pseudo-random, uniformly distributed boolean value.
     */
    public boolean nextBoolean() {
      return next(1) != 0;
    }
    /**
     * Modifies the {@code byte} array by a random sequence of {@code byte}s
     * generated by this random number generator.
     * 
     * @param buf non-null array to contain the new random {@code byte}s.
     * @see #next
     */
    public void nextBytes(byte[] buf) {
      int rand = 0, count = 0, loop = 0;
      while (count < buf.length) {
        if (loop == 0) {
          rand = nextInt();
          loop = 3;
        } else {
          loop--;
        }
        buf[count++] = (byte) rand;
        rand >>= 8;
      }
    }
    /**
     * Generates a normally distributed random {@code double} number between 0.0
     * inclusively and 1.0 exclusively.
     * 
     * @return a random {@code double} in the range [0.0 - 1.0)
     * @see #nextFloat
     */
    public double nextDouble() {
      return ((((long) next(26) << 27) + next(27)) / (double) (1L << 53));
    }
    /**
     * Generates a normally distributed random {@code float} number between 0.0
     * inclusively and 1.0 exclusively.
     * 
     * @return float a random {@code float} number between [0.0 and 1.0)
     * @see #nextDouble
     */
    public float nextFloat() {
      return (next(24) / 16777216f);
    }
    /**
     * Pseudo-randomly generates (approximately) a normally distributed {@code
     * double} value with mean 0.0 and a standard deviation value of {@code 1.0}
     * using the <i>polar method<i> of G. E. P. Box, M. E. Muller, and G.
     * Marsaglia, as described by Donald E. Knuth in <i>The Art of Computer
     * Programming, Volume 2: Seminumerical Algorithms</i>, section 3.4.1,
     * subsection C, algorithm P.
     * 
     * @return a random {@code double}
     * @see #nextDouble
     */
    public double nextGaussian() {
      if (haveNextNextGaussian) {
        // if X1 has been returned, return the second Gaussian
        haveNextNextGaussian = false;
        return nextNextGaussian;
      }
      double v1, v2, s;
      do {
        // Generates two independent random variables U1, U2
        v1 = 2 * nextDouble() - 1;
        v2 = 2 * nextDouble() - 1;
        s = v1 * v1 + v2 * v2;
      } while (s >= 1);
      double norm = Math.sqrt(-2 * Math.log(s) / s);
      nextNextGaussian = v2 * norm;
      haveNextNextGaussian = true;
      return v1 * norm;
    }
    /**
     * Generates a uniformly distributed 32-bit {@code int} value from the random
     * number sequence.
     * 
     * @return a uniformly distributed {@code int} value.
     * @see java.lang.Integer#MAX_VALUE
     * @see java.lang.Integer#MIN_VALUE
     * @see #next
     * @see #nextLong
     */
    public int nextInt() {
      return next(32);
    }
    /**
     * Returns a new pseudo-random {@code int} value which is uniformly
     * distributed between 0 (inclusively) and the value of {@code n}
     * (exclusively).
     * 
     * @param n the exclusive upper border of the range [0 - n).
     * @return a random {@code int}.
     */
    public int nextInt(int n) {
      if (n > 0) {
        if ((n & -n) == n) {
          return (int) ((n * (long) next(31)) >> 31);
        }
        int bits, val;
        do {
          bits = next(31);
          val = bits % n;
        } while (bits - val + (n - 1) < 0);
        return val;
      }
      throw new IllegalArgumentException();
    }
    /**
     * Generates a uniformly distributed 64-bit integer value from the random
     * number sequence.
     * 
     * @return 64-bit random integer.
     * @see java.lang.Integer#MAX_VALUE
     * @see java.lang.Integer#MIN_VALUE
     * @see #next
     * @see #nextInt()
     * @see #nextInt(int)
     */
    public long nextLong() {
      return ((long) next(32) << 32) + next(32);
    }
    /**
     * Modifies the seed a using linear congruential formula presented in <i>The
     * Art of Computer Programming, Volume 2</i>, Section 3.2.1.
     * 
     * @param seed the seed that alters the state of the random number generator.
     * @see #next
     * @see #Random()
     * @see #Random(long)
     */
    public void setSeed(long seed) {
      m_originalSeed = seed;
      this.seed = (seed ^ multiplier) & ((1L << 48) - 1);
      haveNextNextGaussian = false;
    }
    /**
     * Returns a pseudo-random uniformly distributed {@code int} value of the
     * number of bits specified by the argument {@code bits} as described by
     * Donald E. Knuth in <i>The Art of Computer Programming, Volume 2:
     * Seminumerical Algorithms</i>, section 3.2.1.
     * 
     * @param bits number of bits of the returned value.
     * @return a pseudo-random generated int number.
     * @see #nextBytes
     * @see #nextDouble
     * @see #nextFloat
     * @see #nextInt()
     * @see #nextInt(int)
     * @see #nextGaussian
     * @see #nextLong
     */
    protected int next(int bits) {
      seed = (seed * multiplier + 0xbL) & ((1L << 48) - 1);
      return (int) (seed >>> (48 - bits));
    }
    
//    @Override
//    protected int next(int nbits) 
//    {
//      // N.B. Not thread-safe!
//      long x = this.seed;
//      x ^= (x << 21);
//      x ^= (x >>> 35);
//      x ^= (x << 4);
//      this.seed = x;
//      x &= ((1L << nbits) -1);
//      return (int) x;
//    }
    
    public Random2 copy()
    {
        Random2 rnd = new Random2(m_originalSeed);
        rnd.seed = seed;
        rnd.haveNextNextGaussian = haveNextNextGaussian;
        rnd.nextNextGaussian = nextNextGaussian;
        return rnd;
    }

  public static void main(String[] args)
  {
      Random2 rnd1 = new Random2();
      Random2 rnd2 = rnd1.copy();
      for (int i = 0; i < 20; i++)
      {
          long a = rnd1.nextLong();
          long b = rnd2.nextLong();
          if (a != b)
              System.out.println("diff " + a + " != " + b);
      }
      
      rnd2 = rnd1.copy();
      for (int i = 0; i < 20; i++)
      {
          long a = rnd1.nextLong();
          long b = rnd2.nextLong();
          if (a != b)
              System.out.println("diff " + a + " != " + b);
      }
  }

}
