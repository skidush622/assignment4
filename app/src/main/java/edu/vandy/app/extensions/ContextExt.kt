package edu.vandy.app.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.support.annotation.AnyRes

/**
 * Generic mapping of any Android resource ID to a resource Uri.
 */
fun Context.getResourceUri(@AnyRes resId: Int): Uri {
    with(resources) {
        return with(Uri.Builder()) {
            scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            authority(getResourcePackageName(resId))
            appendPath(getResourceTypeName(resId))
            appendPath(getResourceEntryName(resId))
            build()
        }
    }
}
