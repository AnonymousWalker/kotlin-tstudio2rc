package org.wycliffeassociates.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class SourceTranslation(
    @JsonProperty("language_id")
    val languageId: String,
    @JsonProperty("resource_id")
    val resourceId: String,
    @JsonProperty("checking_level")
    val checkingLevel: String,
    @JsonProperty("date_modified")
    val dateModified: String,
    val version: String
)