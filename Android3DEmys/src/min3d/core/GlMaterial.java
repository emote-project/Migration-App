package min3d.core;

import java.nio.FloatBuffer;

public class GlMaterial {
	private float[] amb = {0.6f, 0.6f, 0.6f,1f};
	private float[] diff = {0.6f, 0.6f, 0.6f,1f};
	private float[] spec = {1f, 1f, 1f,1f};
	private float shiny = 50f;
	
	public GlMaterial() {
		//default white
	}
	
	public GlMaterial(float[] amb, float[] diff, float[] spec, float shiny) {
		super();
		this.amb = amb;
		this.diff = diff;
		this.spec = spec;
		this.shiny = shiny;
	}
	public FloatBuffer getAmb() {
		return FloatBuffer.wrap(amb);
	}
	public void setAmb(float[] amb) {
		this.amb = amb;
	}
	public FloatBuffer getDiff() {
		return FloatBuffer.wrap(diff);
	}
	public void setDiff(float[] diff) {
		this.diff = diff;
	}
	public FloatBuffer getSpec() {
		return FloatBuffer.wrap(spec);
	}
	public void setSpec(float[] spec) {
		this.spec = spec;
	}
	public float getShiny() {
		return shiny;
	}
	public void setShiny(float shiny) {
		this.shiny = shiny;
	}
	
	
	
}
