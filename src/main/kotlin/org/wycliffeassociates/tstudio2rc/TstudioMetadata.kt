package org.wycliffeassociates.tstudio2rc

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import org.wycliffeassociates.resourcecontainer.entity.Language
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Source
import org.wycliffeassociates.tstudio2rc.serializable.ProjectManifest
import org.wycliffeassociates.tstudio2rc.serializable.TargetLanguage
import java.io.File
import java.io.InvalidObjectException
import java.time.LocalDate

class TstudioMetadata(path: String) {

    private val projectDir = File(path)
    val manifest: ProjectManifest
    init {
        if (!projectDir.isDirectory) {
            throw IllegalArgumentException("Provided path is not a directory: $path")
        }
        manifest = try {
            parseManifestFile()
        } catch (e: Exception) {
            throw IllegalArgumentException("$path is not a valid project.", e)
        }
    }

    private fun parseManifestFile(): ProjectManifest {
        val file = projectDir.resolve("manifest.json")
        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        return mapper.readValue(file)
    }

    private val sources = manifest.sourceTranslations
        .map {
            Source(it.resourceId, it.languageId, it.version)
        }
        .toMutableList()

    private val dublinCore: DublinCore
        get() = DublinCore(
            type = "book",
            conformsTo = "rc0.2",
            format = "text/${manifest.format}",
            identifier = manifest.resource.id,
            title = manifest.resource.name,
            subject = "Bible",
            description = "",
            language = mapToLanguageEntity(manifest.targetLanguage),
            source = sources,
            rights = "CC BY-SA 4.0",
            creator = "Unknown Creator",
            contributor = manifest.translators.toMutableList(),
            relation = mutableListOf(),
            publisher = "Door43",
            issued = LocalDate.now().toString(),
            modified = LocalDate.now().toString(),
            version = "1"
        )

    val rcProject: RCProject
        get() {
            val projectPath = if (projectDir.resolve("content").isDirectory) "./content" else "./"
            return RCProject(
                identifier = manifest.project.id,
                title = manifest.project.name,
                sort = 1,
                path = projectPath,
                versification = "kjv",
                categories = listOf()
            )
        }

    fun rcManifest(): Manifest {
        return Manifest(
            dublinCore = dublinCore,
            checking = Checking(
                checkingEntity = listOf("Wycliffe Associates"),
                checkingLevel = "1"
            ),
            projects = listOf(rcProject)
        )
    }
}

fun mapToLanguageEntity(targetLanguage: TargetLanguage) = Language(
    identifier = targetLanguage.id,
    title = targetLanguage.name,
    direction = targetLanguage.direction
)