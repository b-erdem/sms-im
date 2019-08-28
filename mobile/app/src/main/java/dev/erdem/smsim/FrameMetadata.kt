package dev.erdem.smsim

/** Describing a frame info.  */
class FrameMetadata private constructor(val width: Int, val height: Int, val rotation: Int, val cameraFacing: Int) {

    /** Builder of [FrameMetadata].  */
    class Builder {

        private var width: Int = 0
        private var height: Int = 0
        private var rotation: Int = 0
        private var cameraFacing: Int = 0

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): Builder {
            this.rotation = rotation
            return this
        }

        fun setCameraFacing(facing: Int): Builder {
            cameraFacing = facing
            return this
        }

        fun build(): FrameMetadata {
            return FrameMetadata(width, height, rotation, cameraFacing)
        }
    }
}