package tachiyomi.core.common.storage

import com.hippo.unifile.UniFile

val UniFile.extension: String?
    get() = name?.substringAfterLast('.')

val UniFile.nameWithoutExtension: String?
    get() = name?.substringBeforeLast('.')

val UniFile.displayablePath: String
    get() = filePath ?: uri.toString()

/**
 * Returns a directory, creating it if it doesn't exist.
 * This is more robust than [UniFile.createDirectory] as it handles cases where
 * the directory might already exist but the provider returns null on creation.
 *
 * @param name The name of the directory to find or create.
 * @return The found or created directory, or null if creation failed.
 */
fun UniFile.getOrCreateDirectory(name: String): UniFile? {
    val existing = findFile(name)
    if (existing != null) {
        return if (existing.isDirectory) existing else null
    }
    return try {
        createDirectory(name)
    } catch (e: Exception) {
        null
    }
}

/**
 * Checks if this UniFile represents a location that might have SAF limitations.
 * Common in the "Downloads" provider on some Android versions where subfolder
 * creation via certain URI types is restricted.
 */
val UniFile.isSafRestricted: Boolean
    get() {
        val authority = uri.authority ?: return false
        val path = uri.path ?: ""
        return authority == "com.android.providers.downloads.documents" ||
            path.contains("primary:Download", ignoreCase = true) ||
            path.contains("/tree/downloads", ignoreCase = true)
    }

/**
 * Checks if the directory is actually writable by verifying permissions
 * and checking if the file system is not read-only.
 */
fun UniFile.canWriteRobust(): Boolean {
    return exists() && isDirectory && canWrite()
}
