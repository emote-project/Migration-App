package min3d.interfaces;

import min3d.core.Object3d;
import min3d.core.Object3dContainer;

/**
 * Using Actionscript 3 nomenclature for what are essentially "pass-thru" methods to an underlying ArrayList  
 */
public interface IObject3dContainer 
{
	public void addChild(Object3dContainer $child);
	public void addChildAt(Object3dContainer $child, int $index);
	public boolean removeChild(Object3dContainer $child);
	public Object3dContainer removeChildAt(int $index);
	public Object3dContainer getChildAt(int $index);
	public Object3dContainer getChildByName(String $string);
	public int getChildIndexOf(Object3dContainer $o);	
	public int numChildren();
	public void removeChild(Object3d object3d);
}
