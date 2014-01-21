package org.terasology.anotherWorld.util;

/**
 * Represents a 3D single-precision Affine transformation.
 * Useful for 3D geometric calculations.
 */
public class Transform implements Cloneable {
    /**
     * row-major matrix (row index is most significant)
     */
    private float[] mat;

    /**
     * Creates a new identity transformation
     */
    public Transform() {
        // allocate an array; init to identity matrix
        mat = new float[]
                {
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1
                };
    }

    /**
     * Create a transform using an existing array
     */
    protected Transform(float[] matrix) {
        mat = matrix;
    }

    /**
     * Creates an independent copy of the transform
     */
    @Override
    public Transform clone() {
        return new Transform(mat.clone());
    }

    /**
     * Retrieve matrix element directly
     */
    public float element(int row, int col) {
        return mat[(row & 3) << 2 | (col & 3)];
    }

    /**
     * Set matrix element directly
     */
    public void setElement(int row, int col, float value) {
        mat[(row & 3) << 2 | (col & 3)] = value;
    }

    /**
     * Get row-major 4x4 backing array
     */
    public float[] elements() {
        return mat;
    }

    /**
     * Reset matrix to identity
     */
    public Transform identity() {
        mat[0] = 1;
        mat[1] = 0;
        mat[2] = 0;
        mat[3] = 0;
        mat[4] = 0;
        mat[5] = 1;
        mat[6] = 0;
        mat[7] = 0;
        mat[8] = 0;
        mat[9] = 0;
        mat[10] = 1;
        mat[11] = 0;
        mat[12] = 0;
        mat[13] = 0;
        mat[14] = 0;
        mat[15] = 1;
        return this;
    }

    /**
     * In-place local transformation: new = old * transformation
     */
    public Transform transform(final Transform trans) {
        mult(mat, trans.mat);
        return this;
    }

    /**
     * Apply transformation to local vector:  result_vector = this_trans * vector
     *
     * @param vector The vector as (x,y,z) or (x,y,z,w)
     */
    public void transformVector(float[] vector) {
        float vw = (vector.length > 3) ? vector[3] : 1;
        // right-multiply: matrix * vector
        float x = mat[0] * vector[0] + mat[1] * vector[1] + mat[2] * vector[2] + mat[3] * vw;
        float y = mat[4] * vector[0] + mat[5] * vector[1] + mat[6] * vector[2] + mat[7] * vw;
        float z = mat[8] * vector[0] + mat[9] * vector[1] + mat[10] * vector[2] + mat[11] * vw;
        float w = mat[12] * vector[0] + mat[13] * vector[1] + mat[14] * vector[2] + mat[15] * vw;
        /* FOR FUTURE USE:
        // left-multiply: vector * matrix
		float x = vector[0]*mat[0] + vector[1]*mat[4] + vector[2]*mat[8]  + vw*mat[12];
		float y = vector[0]*mat[1] + vector[1]*mat[5] + vector[2]*mat[9]  + vw*mat[13];
		float z = vector[0]*mat[2] + vector[1]*mat[6] + vector[2]*mat[10] + vw*mat[14];
		float w = vector[0]*mat[3] + vector[1]*mat[7] + vector[2]*mat[11] + vw*mat[15];		
		*/
        // place results in original vector
        vector[0] = x;
        vector[1] = y;
        vector[2] = z;
        if (vector.length > 3) {
            vector[3] = w;
        }
    }

    /**
     * Apply transformation to a list of local vectors
     *
     * @param vectors Array containing consecutive vectors.  Coordinate order is {x,y,z,w}.
     * @param size    Number of coordinates per vector.  Must be between 1 and 4.
     * @param base    Starting index in array
     * @param count   Number of vectors to read from array
     * @throws RuntimeError if size is invalid or array is too short to contain the requested vectors.
     */
    public void transformVectors(float[] vectors, int size, int base, int count) {
        // validate params
        if (size < 1 || size > 4) {
            throw new RuntimeException("Attempting to transform vectors of invalid size.");
        }
        if (vectors.length < base + count * size) {
            throw new RuntimeException("Attempting to transform vector array that is too short.");
        }
        // loop through vectors
        for (int offset = base; offset < base + count * size; offset += size) {
            // get base coordinates
            float vx = vectors[offset + 0];
            float vy = (size > 1) ? vectors[offset + 1] : 0;
            float vz = (size > 2) ? vectors[offset + 2] : 0;
            float vw = (size > 3) ? vectors[offset + 3] : 1;
            // right-multiply: matrix * vector
            vectors[offset + 0] = mat[0] * vx + mat[1] * vy + mat[2] * vz + mat[3] * vw;
            if (size > 1) {
                vectors[offset + 1] = mat[4] * vx + mat[5] * vy + mat[6] * vz + mat[7] * vw;
            }
            if (size > 2) {
                vectors[offset + 2] = mat[8] * vx + mat[9] * vy + mat[10] * vz + mat[11] * vw;
            }
            if (size > 3) {
                vectors[offset + 3] = mat[12] * vx + mat[13] * vy + mat[14] * vz + mat[15] * vw;
            }
        }
    }

    /**
     * Apply transformation to a local bounding box, returning a new bounding box guaranteed to contain the original.
     *
     * @param bounds The bounding box as (minX, minY, minZ, maxX, maxY, maxZ)
     */
    public void transformBB(float[] bounds) {
        float[] v = new float[3];
        // initialize new bound values
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        // test each of the eight corners
        for (int c = 0; c < 8; c++) {
            // permute coordinates
            v[0] = bounds[(c & 1) == 0 ? 0 : 3];
            v[1] = bounds[(c & 2) == 0 ? 1 : 4];
            v[2] = bounds[(c & 4) == 0 ? 2 : 5];
            // transform
            transformVector(v);
            // update bounds
            if (v[0] < minX) {
                minX = v[0];
            }
            if (v[1] < minY) {
                minY = v[1];
            }
            if (v[2] < minZ) {
                minZ = v[2];
            }
            if (v[0] > maxX) {
                maxX = v[0];
            }
            if (v[1] > maxY) {
                maxY = v[1];
            }
            if (v[2] > maxZ) {
                maxZ = v[2];
            }
        }
        // place results in original vector
        bounds[0] = minX;
        bounds[1] = minY;
        bounds[2] = minZ;
        bounds[3] = maxX;
        bounds[4] = maxY;
        bounds[5] = maxZ;
    }

    /**
     * In-place rotation about arbitrary local axis through the origin.
     *
     * @throws RuntimeException if the provided axis is null
     */
    public Transform rotate(float angle, float axisX, float axisY, float axisZ) {
        // normalize axis
        float r = axisX * axisX + axisY * axisY + axisZ * axisZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to rotate about a null vector");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            axisX /= r;
            axisY /= r;
            axisZ /= r;
        }
        // cache sin & cosine of angles
        float s = (float) Math.sin(angle);
        float nc = 1 - (float) Math.cos(angle);
        float[] rot = new float[16];
        //
        rot[0] = 1 + (axisX * axisX - 1) * nc;
        rot[1] = axisX * axisY * nc - axisZ * s;
        rot[2] = axisX * axisZ * nc + axisY * s;
        //
        rot[4] = axisX * axisY * nc + axisZ * s;
        rot[5] = 1 + (axisY * axisY - 1) * nc;
        rot[6] = axisY * axisZ * nc - axisX * s;
        //
        rot[8] = axisX * axisZ * nc - axisY * s;
        rot[9] = axisY * axisZ * nc + axisX * s;
        rot[10] = 1 + (axisZ * axisZ - 1) * nc;
        //
        rot[15] = 1;
        // perform multiplication
        mult(mat, rot);
        return this;
    }

    /**
     * In-place rotation about local X axis
     */
    public Transform rotateX(float angle) {
        // cache sin & cosine of angles
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float tmp = 0;
        // in-place mixing of Y/Z columns
        tmp = mat[1];
        mat[1] = tmp * c + mat[2] * s;
        mat[2] = mat[2] * c - tmp * s;
        //
        tmp = mat[5];
        mat[5] = tmp * c + mat[6] * s;
        mat[6] = mat[6] * c - tmp * s;
        //
        tmp = mat[9];
        mat[9] = tmp * c + mat[10] * s;
        mat[10] = mat[10] * c - tmp * s;
        //
        tmp = mat[13];
        mat[13] = tmp * c + mat[14] * s;
        mat[14] = mat[14] * c - tmp * s;
        //
        return this;
    }

    /**
     * In-place rotation about local Y axis
     */
    public Transform rotateY(float angle) {
        // cache sin & cosine of angles
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float tmp = 0;
        // in-place mixing of X/Z columns
        tmp = mat[0];
        mat[0] = tmp * c - mat[2] * s;
        mat[2] = mat[2] * c + tmp * s;
        //
        tmp = mat[4];
        mat[4] = tmp * c - mat[6] * s;
        mat[6] = mat[6] * c + tmp * s;
        //
        tmp = mat[8];
        mat[8] = tmp * c - mat[10] * s;
        mat[10] = mat[10] * c + tmp * s;
        //
        tmp = mat[12];
        mat[12] = tmp * c - mat[14] * s;
        mat[14] = mat[14] * c + tmp * s;
        //
        return this;
    }

    /**
     * In-place rotation about local Z axis
     */
    public Transform rotateZ(float angle) {
        // cache sin & cosine of angles
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float tmp = 0;
        // in-place mixing of X/Y columns
        tmp = mat[0];
        mat[0] = tmp * c + mat[1] * s;
        mat[1] = mat[1] * c - tmp * s;
        //
        tmp = mat[4];
        mat[4] = tmp * c + mat[5] * s;
        mat[5] = mat[5] * c - tmp * s;
        //
        tmp = mat[8];
        mat[8] = tmp * c + mat[9] * s;
        mat[9] = mat[9] * c - tmp * s;
        //
        tmp = mat[12];
        mat[12] = tmp * c + mat[13] * s;
        mat[13] = mat[13] * c - tmp * s;
        //
        return this;
    }

    /**
     * In-place rotation to set the local X axis to the local axis provided
     *
     * @throws RuntimeException if the provided axis is null
     */
    public Transform rotateXInto(float axisX, float axisY, float axisZ) {
        // normalize axis
        float r = axisX * axisX + axisY * axisY + axisZ * axisZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to rotate into a null vector");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            axisX /= r;
            axisY /= r;
            axisZ /= r;
        }
        // cache squared norm of cross product (equal to sin^2 of rotation angle)
        float s2 = axisY * axisY + axisZ * axisZ;
        if (s2 == 0) {
            return this; // zero rotation angle -> no transformation
        }
        float[] rot = new float[16];
        //
        rot[0] = axisX;
        rot[1] = -axisY;
        rot[2] = -axisZ;
        //
        rot[4] = axisY;
        rot[5] = (axisY * axisY * axisX + axisZ * axisZ) / s2;
        rot[6] = axisY * axisZ * (axisX - 1) / s2;
        //
        rot[8] = axisZ;
        rot[9] = axisY * axisZ * (axisX - 1) / s2;
        rot[10] = (axisY * axisY + axisZ * axisZ * axisX) / s2;
        //
        rot[15] = 1;
        // perform multiplication
        mult(mat, rot);
        return this;
    }

    /**
     * In-place rotation to set the local Y axis to the local axis provided
     *
     * @throws RuntimeException if the provided axis is null
     */
    public Transform rotateYInto(float axisX, float axisY, float axisZ) {
        // normalize axis
        float r = axisX * axisX + axisY * axisY + axisZ * axisZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to rotate into a null vector");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            axisX /= r;
            axisY /= r;
            axisZ /= r;
        }
        // cache squared norm of cross product (equal to sin^2 of rotation angle)
        float s2 = axisX * axisX + axisZ * axisZ;
        if (s2 == 0) {
            return this; // zero rotation angle -> no transformation
        }
        float[] rot = new float[16];
        //
        rot[0] = (axisX * axisX * axisY + axisZ * axisZ) / s2;
        rot[1] = axisX;
        rot[2] = axisX * axisZ * (axisY - 1) / s2;
        //
        rot[4] = -axisX;
        rot[5] = axisY;
        rot[6] = -axisZ;
        //
        rot[8] = axisX * axisZ * (axisY - 1) / s2;
        rot[9] = axisZ;
        rot[10] = (axisX * axisX + axisZ * axisZ * axisY) / s2;
        //
        rot[15] = 1;
        // perform multiplication
        mult(mat, rot);
        return this;
    }

    /**
     * In-place rotation to set the local Z axis to the local axis provided
     *
     * @throws RuntimeException if the provided axis is null
     */
    public Transform rotateZInto(float axisX, float axisY, float axisZ) {
        // normalize axis
        float r = axisX * axisX + axisY * axisY + axisZ * axisZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to rotate into a null vector");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            axisX /= r;
            axisY /= r;
            axisZ /= r;
        }
        // cache squared norm of cross product (equal to sin^2 of rotation angle)
        float s2 = axisX * axisX + axisY * axisY;
        if (s2 == 0) {
            return this; // zero rotation angle -> no transformation
        }
        float[] rot = new float[16];
        //
        rot[0] = (axisX * axisX * axisZ + axisY * axisY) / s2;
        rot[1] = axisX * axisY * (axisZ - 1) / s2;
        rot[2] = axisX;
        //
        rot[4] = axisX * axisY * (axisZ - 1) / s2;
        rot[5] = (axisX * axisX + axisY * axisY * axisZ) / s2;
        rot[6] = axisY;
        //
        rot[8] = -axisX;
        rot[9] = -axisY;
        rot[10] = axisZ;
        //
        rot[15] = 1;
        // perform multiplication
        mult(mat, rot);
        return this;
    }

    /**
     * In-place scaling along arbitrary local axis through the origin.
     *
     * @throws RuntimeException if the provided axis is null
     */
    public Transform scale(float scaleM, float axisX, float axisY, float axisZ) {
        // normalize axis
        float r = axisX * axisX + axisY * axisY + axisZ * axisZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to scale along a null vector");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            axisX /= r;
            axisY /= r;
            axisZ /= r;
        }
        // modify scale for convenience
        scaleM -= 1;
        float[] scal = new float[16];
        //
        scal[0] = scaleM * axisX * axisX + 1;
        scal[1] = scaleM * axisX * axisY;
        scal[2] = scaleM * axisX * axisZ;
        //
        scal[4] = scaleM * axisX * axisY;
        scal[5] = scaleM * axisY * axisY + 1;
        scal[6] = scaleM * axisY * axisZ;
        //
        scal[8] = scaleM * axisX * axisZ;
        scal[9] = scaleM * axisY * axisZ;
        scal[10] = scaleM * axisZ * axisZ + 1;
        //
        scal[15] = 1;
        // perform multiplication
        mult(mat, scal);
        return this;
    }

    /**
     * In-place scaling along canonical local axes
     */
    public Transform scale(float scaleX, float scaleY, float scaleZ) {
        // in-place update of matrix
        mat[0] *= scaleX;
        mat[1] *= scaleY;
        mat[2] *= scaleZ;
        mat[4] *= scaleX;
        mat[5] *= scaleY;
        mat[6] *= scaleZ;
        mat[8] *= scaleX;
        mat[9] *= scaleY;
        mat[10] *= scaleZ;
        mat[12] *= scaleX;
        mat[13] *= scaleY;
        mat[14] *= scaleZ;
        //
        return this;
    }

    /**
     * In-place shearing along arbitrary local axes through the origin.
     *
     * @throws RuntimeException if the axes are null or parallel to each other
     */
    public Transform shear(float angle, float shearX, float shearY, float shearZ, float invariantX, float invariantY, float invariantZ) {
        // normalize invariant axis
        float ri = invariantX * invariantX + invariantY * invariantY + invariantZ * invariantZ;
        if (ri == 0) {
            throw new RuntimeException("Attempting to shear with a null invariant vector");
        }
        if (ri != 1) {
            ri = (float) Math.sqrt(ri);
            invariantX /= ri;
            invariantY /= ri;
            invariantZ /= ri;
        }
        // orthogonalize shear axis
        float p = shearX * invariantX + shearY * invariantY + shearZ * invariantZ;
        if (p != 0) {
            shearX -= p * invariantX;
            shearY -= p * invariantY;
            shearZ -= p * invariantZ;
        }
        // normalize shear axis
        float rs = shearX * shearX + shearY * shearY + shearZ * shearZ;
        if (rs == 0) {
            throw new RuntimeException("Attempting to shear with a null or parallel shear vector");
        }
        if (rs != 1) {
            rs = (float) Math.sqrt(rs);
            shearX /= rs;
            shearY /= rs;
            shearZ /= rs;
        }
        // cache tan of angle
        float t = (float) Math.tan(angle);
        float[] shr = new float[16];
        //
        shr[0] = shearX * invariantX * t + 1;
        shr[1] = shearX * invariantY * t;
        shr[2] = shearX * invariantZ * t;
        //
        shr[4] = shearY * invariantX * t;
        shr[5] = shearY * invariantY * t + 1;
        shr[6] = shearY * invariantZ * t;
        //
        shr[8] = shearZ * invariantX * t;
        shr[9] = shearZ * invariantY * t;
        shr[10] = shearZ * invariantZ * t + 1;
        //
        shr[15] = 1;
        // perform multiplication
        mult(mat, shr);
        return this;
    }

    /**
     * In-place reflection across arbitrary local plane through the origin.
     *
     * @throws RuntimeException if the plane normal vector is null
     */
    public Transform reflect(float mirrorNormalX, float mirrorNormalY, float mirrorNormalZ) {
        // normalize axis
        float r = mirrorNormalX * mirrorNormalX + mirrorNormalY * mirrorNormalY + mirrorNormalZ * mirrorNormalZ;
        if (r == 0) {
            throw new RuntimeException("Attempting to reflect across a null plane");
        }
        if (r != 1) {
            r = (float) Math.sqrt(r);
            mirrorNormalX /= r;
            mirrorNormalY /= r;
            mirrorNormalZ /= r;
        }
        //
        float[] refl = new float[16];
        //
        refl[0] = 1 - 2 * mirrorNormalX * mirrorNormalX;
        refl[1] = -2 * mirrorNormalX * mirrorNormalY;
        refl[2] = -2 * mirrorNormalX * mirrorNormalZ;
        //
        refl[4] = -2 * mirrorNormalX * mirrorNormalY;
        refl[5] = 1 - 2 * mirrorNormalY * mirrorNormalY;
        refl[6] = -2 * mirrorNormalY * mirrorNormalZ;
        //
        refl[8] = -2 * mirrorNormalX * mirrorNormalZ;
        refl[9] = -2 * mirrorNormalY * mirrorNormalZ;
        refl[10] = 1 - 2 * mirrorNormalZ * mirrorNormalZ;
        //
        refl[15] = 1;
        // perform multiplication
        mult(mat, refl);
        return this;
    }

    /**
     * In-place translation along local axes
     */
    public Transform translate(float transX, float transY, float transZ) {
        // in-place update of matrix
        mat[3] += mat[0] * transX + mat[1] * transY + mat[2] * transZ;
        mat[7] += mat[4] * transX + mat[5] * transY + mat[6] * transZ;
        mat[11] += mat[8] * transX + mat[9] * transY + mat[10] * transZ;
        mat[15] += mat[12] * transX + mat[13] * transY + mat[14] * transZ;
        //
        return this;
    }

    /**
     * Calculate the matrix determinant
     */
    public float determinant() {
        return mat[0] * (mat[5] * (mat[10] * mat[15] - mat[11] * mat[14]) + mat[6] * (mat[11] * mat[13] - mat[15] * mat[9]) + mat[7] * (mat[14] * mat[9] - mat[10] * mat[13]))
                + mat[1] * (mat[4] * (mat[11] * mat[14] - mat[10] * mat[15]) + mat[6] * (mat[15] * mat[8] - mat[11] * mat[12]) + mat[7] * (mat[10] * mat[12] - mat[14] * mat[8]))
                + mat[2] * (mat[4] * (mat[15] * mat[9] - mat[11] * mat[13]) + mat[5] * (mat[11] * mat[12] - mat[15] * mat[8]) + mat[7] * (mat[13] * mat[8] - mat[12] * mat[9]))
                + mat[3] * (mat[4] * (mat[10] * mat[13] - mat[14] * mat[9]) + mat[5] * (mat[14] * mat[8] - mat[10] * mat[12]) + mat[6] * (mat[12] * mat[9] - mat[13] * mat[8]));
    }

    /**
     * In-place matrix inversion
     *
     * @throws RuntimeException if the transformation is singular
     */
    public Transform inverse() {
        float det = determinant();
        if (det == 0) {
            throw new RuntimeException("Attempting to invert a singular matrix");
        }
        //
        float n00 = mat[5] * (mat[10] * mat[15] - mat[11] * mat[14]) + mat[6] * (mat[11] * mat[13] - mat[15] * mat[9]) + mat[7] * (mat[14] * mat[9] - mat[10] * mat[13]);
        float n01 = mat[1] * (mat[11] * mat[14] - mat[10] * mat[15]) + mat[2] * (mat[15] * mat[9] - mat[11] * mat[13]) + mat[3] * (mat[10] * mat[13] - mat[14] * mat[9]);
        float n02 = mat[1] * (mat[15] * mat[6] - mat[14] * mat[7]) + mat[2] * (mat[13] * mat[7] - mat[15] * mat[5]) + mat[3] * (mat[14] * mat[5] - mat[13] * mat[6]);
        float n03 = mat[1] * (mat[10] * mat[7] - mat[11] * mat[6]) + mat[2] * (mat[11] * mat[5] - mat[7] * mat[9]) + mat[3] * (mat[6] * mat[9] - mat[10] * mat[5]);
        //
        float n04 = mat[4] * (mat[11] * mat[14] - mat[10] * mat[15]) + mat[6] * (mat[15] * mat[8] - mat[11] * mat[12]) + mat[7] * (mat[10] * mat[12] - mat[14] * mat[8]);
        float n05 = mat[0] * (mat[10] * mat[15] - mat[11] * mat[14]) + mat[2] * (mat[11] * mat[12] - mat[15] * mat[8]) + mat[3] * (mat[14] * mat[8] - mat[10] * mat[12]);
        float n06 = mat[0] * (mat[14] * mat[7] - mat[15] * mat[6]) + mat[2] * (mat[15] * mat[4] - mat[12] * mat[7]) + mat[3] * (mat[12] * mat[6] - mat[14] * mat[4]);
        float n07 = mat[0] * (mat[11] * mat[6] - mat[10] * mat[7]) + mat[2] * (mat[7] * mat[8] - mat[11] * mat[4]) + mat[3] * (mat[10] * mat[4] - mat[6] * mat[8]);
        //
        float n08 = mat[4] * (mat[15] * mat[9] - mat[11] * mat[13]) + mat[5] * (mat[11] * mat[12] - mat[15] * mat[8]) + mat[7] * (mat[13] * mat[8] - mat[12] * mat[9]);
        float n09 = mat[0] * (mat[11] * mat[13] - mat[15] * mat[9]) + mat[1] * (mat[15] * mat[8] - mat[11] * mat[12]) + mat[3] * (mat[12] * mat[9] - mat[13] * mat[8]);
        float n10 = mat[0] * (mat[15] * mat[5] - mat[13] * mat[7]) + mat[1] * (mat[12] * mat[7] - mat[15] * mat[4]) + mat[3] * (mat[13] * mat[4] - mat[12] * mat[5]);
        float n11 = mat[0] * (mat[7] * mat[9] - mat[11] * mat[5]) + mat[1] * (mat[11] * mat[4] - mat[7] * mat[8]) + mat[3] * (mat[5] * mat[8] - mat[4] * mat[9]);
        //
        float n12 = mat[4] * (mat[10] * mat[13] - mat[14] * mat[9]) + mat[5] * (mat[14] * mat[8] - mat[10] * mat[12]) + mat[6] * (mat[12] * mat[9] - mat[13] * mat[8]);
        float n13 = mat[0] * (mat[14] * mat[9] - mat[10] * mat[13]) + mat[1] * (mat[10] * mat[12] - mat[14] * mat[8]) + mat[2] * (mat[13] * mat[8] - mat[12] * mat[9]);
        float n14 = mat[0] * (mat[13] * mat[6] - mat[14] * mat[5]) + mat[1] * (mat[14] * mat[4] - mat[12] * mat[6]) + mat[2] * (mat[12] * mat[5] - mat[13] * mat[4]);
        float n15 = mat[0] * (mat[10] * mat[5] - mat[6] * mat[9]) + mat[1] * (mat[6] * mat[8] - mat[10] * mat[4]) + mat[2] * (mat[4] * mat[9] - mat[5] * mat[8]);
        //
        mat[0] = n00 / det;
        mat[1] = n01 / det;
        mat[2] = n02 / det;
        mat[3] = n03 / det;
        mat[4] = n04 / det;
        mat[5] = n05 / det;
        mat[6] = n06 / det;
        mat[7] = n07 / det;
        mat[8] = n08 / det;
        mat[9] = n09 / det;
        mat[10] = n10 / det;
        mat[11] = n11 / det;
        mat[12] = n12 / det;
        mat[13] = n13 / det;
        mat[14] = n14 / det;
        mat[15] = n15 / det;
        //
        return this;
    }

    /**
     * In-place matrix transposition
     */
    public Transform transpose() {
        float temp = 0;
        // in-place transposition of matrix
        temp = mat[1];
        mat[1] = mat[4];
        mat[4] = temp;
        temp = mat[2];
        mat[2] = mat[8];
        mat[8] = temp;
        temp = mat[3];
        mat[3] = mat[12];
        mat[12] = temp;
        temp = mat[6];
        mat[6] = mat[9];
        mat[9] = temp;
        temp = mat[7];
        mat[7] = mat[13];
        mat[13] = temp;
        temp = mat[11];
        mat[11] = mat[14];
        mat[14] = temp;
        //
        return this;
    }

    /**
     * String representation (multi-line)
     */
    @Override
    public String toString() {
        return String.format("{%#7.4f,%#7.4f,%#7.4f,%#7.4f},\n{%#7.4f,%#7.4f,%#7.4f,%#7.4f},\n{%#7.4f,%#7.4f,%#7.4f,%#7.4f},\n{%#7.4f,%#7.4f,%#7.4f,%#7.4f}",
                mat[0], mat[1], mat[2], mat[3], mat[4], mat[5], mat[6], mat[7], mat[8], mat[9], mat[10], mat[11], mat[12], mat[13], mat[14], mat[15]);
    }

    /**
     * In-place matrix/matrix multiplication:  base = base * mult
     */
    protected static void mult(float[] base, final float[] mult) {
        //
        float n00 = base[0] * mult[0] + base[1] * mult[4] + base[2] * mult[8] + base[3] * mult[12];
        float n01 = base[0] * mult[1] + base[1] * mult[5] + base[2] * mult[9] + base[3] * mult[13];
        float n02 = base[0] * mult[2] + base[1] * mult[6] + base[2] * mult[10] + base[3] * mult[14];
        float n03 = base[0] * mult[3] + base[1] * mult[7] + base[2] * mult[11] + base[3] * mult[15];
        //
        float n04 = base[4] * mult[0] + base[5] * mult[4] + base[6] * mult[8] + base[7] * mult[12];
        float n05 = base[4] * mult[1] + base[5] * mult[5] + base[6] * mult[9] + base[7] * mult[13];
        float n06 = base[4] * mult[2] + base[5] * mult[6] + base[6] * mult[10] + base[7] * mult[14];
        float n07 = base[4] * mult[3] + base[5] * mult[7] + base[6] * mult[11] + base[7] * mult[15];
        //
        float n08 = base[8] * mult[0] + base[9] * mult[4] + base[10] * mult[8] + base[11] * mult[12];
        float n09 = base[8] * mult[1] + base[9] * mult[5] + base[10] * mult[9] + base[11] * mult[13];
        float n10 = base[8] * mult[2] + base[9] * mult[6] + base[10] * mult[10] + base[11] * mult[14];
        float n11 = base[8] * mult[3] + base[9] * mult[7] + base[10] * mult[11] + base[11] * mult[15];
        //
        float n12 = base[12] * mult[0] + base[13] * mult[4] + base[14] * mult[8] + base[15] * mult[12];
        float n13 = base[12] * mult[1] + base[13] * mult[5] + base[14] * mult[9] + base[15] * mult[13];
        float n14 = base[12] * mult[2] + base[13] * mult[6] + base[14] * mult[10] + base[15] * mult[14];
        float n15 = base[12] * mult[3] + base[13] * mult[7] + base[14] * mult[11] + base[15] * mult[15];
        //
        base[0] = n00;
        base[1] = n01;
        base[2] = n02;
        base[3] = n03;
        base[4] = n04;
        base[5] = n05;
        base[6] = n06;
        base[7] = n07;
        base[8] = n08;
        base[9] = n09;
        base[10] = n10;
        base[11] = n11;
        base[12] = n12;
        base[13] = n13;
        base[14] = n14;
        base[15] = n15;
    }
}
