public class Chromosome implements Cloneable
{
	public double[] object;
	public double[] mutationStepSizes;
	// angles
	public double fitness;
	public int lifetime;
	public int age;
	public Chromosome(){
		object = new double[10];
		mutationStepSizes = new double[10];
		fitness = Double.NEGATIVE_INFINITY;
	}

	// copies the fields of this object to another
//	public void copy(Chromosome c) {
//		c.object = new double[object.length];
//		System.arraycopy(object, 0, c.object, 0, object.length);
//
//		c.mutationStepSizes = new double[mutationStepSizes.length];
//		System.arraycopy(mutationStepSizes, 0, c.mutationStepSizes, 0, mutationStepSizes.length);
//
//		c.fitness = fitness;
//	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
	    Chromosome cloned = (Chromosome)super.clone();
	    cloned.object = object.clone();
	    cloned.mutationStepSizes = mutationStepSizes.clone();
		return cloned;
	}

	@Override
	public String toString() {
		String out = "";

		for (double o : object) {
			out += String.format("%f", o) + "|";
		}

		return out;
	}

}
