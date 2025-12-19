package funnyblockdoormod.funnyblockdoormod.data.bvh

import funnyblockdoormod.funnyblockdoormod.data.ffShape.FFComponent
import funnyblockdoormod.funnyblockdoormod.data.ffShape.shape.IShape
import net.minecraft.util.math.BlockPos

class BVHNode {
    var left: BVHNode? = null
    var right: BVHNode? = null
    var isLeaf: Boolean = false
    var dirty: Boolean = false
    var parent: BVHNode? = null


    private var _boundingBox: FFComponent? = null
    var boundingBox: FFComponent?
        get() {
            if (dirty) {
                if (isLeaf) {
                    dirty = false
                    return _boundingBox
                }
                // pull children's bounding boxes (this will lazily update them as needed)
                val leftBB = left?.boundingBox
                val rightBB = right?.boundingBox

                _boundingBox = when {
                    //leftBB == null && rightBB == null -> null shouldn't happen
                    leftBB == null -> rightBB
                    rightBB == null -> leftBB
                    else -> mergeBounds(leftBB, rightBB)
                }
                dirty = false
            }
            return _boundingBox
        }
        set(value) {
            _boundingBox = value
            if (isLeaf){
                setDirty(this)
            }
        }

    fun containsPoint(pos: BlockPos): Boolean {
        return boundingBox?.shape?.containsPoint(pos) ?: false
    }

    fun intersectsBroad(testShape: IShape): Boolean {
        val thisBB = boundingBox ?: return false
        return thisBB.shape.intersects(testShape.getBoundingBox())
    }

    fun intersectsNarrow(testShape: IShape): Boolean {
        val thisBB = boundingBox ?: return false
        return thisBB.shape.intersects(testShape)
    }

    companion object {

        fun mergeBounds(left: FFComponent, right: FFComponent) =
            FFComponent(left.shape.getBoundingBox().union(right.shape.getBoundingBox()))

        fun setDirty(node: BVHNode) {
            node.dirty = true
            var parent = node.parent
            while (parent != null && !parent.dirty) {
                parent.dirty = true
                parent = parent.parent
            }
        }
    }
}
