public class Chromosome implements Cloneable
{
	public double[] object;
	public double[] mutationStepSizes;
	// angles
	public double fitness;
	public double lifetime;
	public double age;
	public Chromosome(){
		object = new double[10];
		mutationStepSizes = new double[10];
		fitness = Double.NEGATIVE_INFINITY;
	}
	
	// copies the fields of this object to another
	public void copy(Chromosome c) {
		c.object = new double[object.length];
		System.arraycopy(object, 0, c.object, 0, object.length);
		
		c.mutationStepSizes = new double[mutationStepSizes.length];
		System.arraycopy(mutationStepSizes, 0, c.mutationStepSizes, 0, mutationStepSizes.length);
		
		c.fitness = fitness;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}
	
	@Override
	public String toString() {
		String out = "Chromosome:\n-fitness: " + fitness;
		
		out += "\n-object: ";
		for (double o : object) {
			out += String.format("%6.3e", o) + "|";
		}
		
		out += "\n-mutationStepSizes: ";
		for (double m : mutationStepSizes) {
			out += String.format("%6.3e", m) + "|";
		}
		
		return out;
	}
	
}