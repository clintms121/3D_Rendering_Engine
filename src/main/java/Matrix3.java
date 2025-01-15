public class Matrix3 {
    double[] values;
    Matrix3(double[] values){
        this.values = values;
    }
    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }
    Vertex transform(Vertex in){
        return new Vertex(
                in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
public void applyTransformation(double[] x, double[] y) {
    double heading = Math.toRadians(x[0]);
    Matrix3 headingTransform = new Matrix3(new double[]{
            Math.cos(heading), 0, -Math.sin(heading),
            0, 1, 0,
            Math.sin(heading), 0, Math.cos(heading)
    });
    double pitch = Math.toRadians(y[0]);
    Matrix3 pitchTransform = new Matrix3(new double[]{
            1, 0, 0,
            0, Math.cos(pitch), Math.sin(pitch),
            0, -Math.sin(pitch), Math.cos(pitch)
    });
    // Merge matrices in advance
    Matrix3 transform = headingTransform.multiply(pitchTransform);
}


}
