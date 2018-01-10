package util;

/**
 * Represents a 3-dimensional vector in a cartesian coordinate system.
 */
class Vector3D {
	public double x, y, z;

	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getNorm() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public Vector3D getDifference(Vector3D second) {
		return new Vector3D(x - second.x, y - second.y, z - second.z);
	}

	public String toString() {
		return "(" + this.x + ", " + this.y + ", " + this.z + ")";
	}
}
