package dnhieuhuy.hoanghuy.robotai.cloudvisionuntils

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.IOException
import java.util.HashMap

/**
 * Created by Administrator on 04/10/2017.
 */
class CloudVisionUtils {

    companion object {
        val TAG = CloudVisionUtils::class.java.simpleName

        //*****************GOOGLE CLOUD VISION IS IN HERE*************************
        private val CLOUD_VISION_API_KEY = "YOUR GOOGLE VISION KEY IS HERE"

        private val LABEL_DETECTION = "LABEL_DETECTION"

        private val MAX_LABEL_RESULTS = 10

        /**
         * Construct an annotated image request for the provided image to be executed
         * using the provided API interface.
         *
         * @param imageBytes image bytes in JPEG format.
         * @return collection of annotation descriptions and scores.
         */
        @Throws(IOException::class)
        fun annotateImage(imageBytes: ByteArray): Map<String, Float> {
            // Construct the Vision API instance
            val httpTransport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val initializer = VisionRequestInitializer(CLOUD_VISION_API_KEY)
            val vision = Vision.Builder(httpTransport, jsonFactory, null)
                    .setVisionRequestInitializer(initializer)
                    .build()

            // Create the image request
            val imageRequest = AnnotateImageRequest()
            val img = Image()
            img.encodeContent(imageBytes)
            imageRequest.setImage(img)

            // Add the features we want
            val labelDetection = Feature()
            labelDetection.setType(LABEL_DETECTION)
            labelDetection.setMaxResults(MAX_LABEL_RESULTS)
            imageRequest.setFeatures(listOf<Feature>(labelDetection))

            // Batch and execute the request
            val requestBatch = BatchAnnotateImagesRequest()
            requestBatch.setRequests(listOf<AnnotateImageRequest>(imageRequest))
            val response = vision.images()
                    .annotate(requestBatch)
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    .setDisableGZipContent(true)
                    .execute()

            return convertResponseToMap(response)
        }

        /**
         * Process an encoded image and return a collection of vision
         * annotations describing features of the image data.
         *
         * @return collection of annotation descriptions and scores.
         */
        private fun convertResponseToMap(response: BatchAnnotateImagesResponse): Map<String, Float> {

            // Convert response into a readable collection of annotations
            val annotations = HashMap<String, Float>()
            val labels = response.getResponses().get(0).getLabelAnnotations()
            if (labels != null) {
                for (label in labels) {
                    annotations.put(label.getDescription(), label.getScore())
                }
            }

            Log.d(TAG, "Cloud Vision request completed:" + annotations)
            return annotations
        }
    }

}
