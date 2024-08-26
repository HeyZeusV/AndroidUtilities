package com.heyzeusv.androidutilitieslibrary.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.heyzeusv.androidutilities.compose.about.overview.StringInfoEntry
import com.heyzeusv.androidutilities.compose.about.overview.StringResourceInfoEntry
import com.heyzeusv.androidutilitieslibrary.R
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.entity.Organization
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

const val lorem = "Lorem ipsum odor amet, consectetuer adipiscing elit. Sapien scelerisque porttitor morbi nunc efficitur feugiat. Hac risus integer morbi hac amet molestie hendrerit turpis leo. Arcu scelerisque quisque ornare adipiscing est fringilla, sociosqu condimentum habitant. Auctor quam pulvinar lorem lectus mi metus tincidunt. Potenti sem pharetra vulputate hac congue. Iaculis neque varius sapien augue quis; aptent a morbi. Nostra faucibus porttitor ultrices penatibus aenean ultricies.\n" +
        "\n" +
        "Sagittis nascetur suscipit rhoncus iaculis lobortis condimentum molestie? Tellus mi nascetur parturient eu vulputate sociosqu. Nulla proin eleifend parturient montes dui luctus pulvinar ex. Pellentesque litora mus curabitur fermentum tristique nisi volutpat consectetur. Purus pretium nascetur fringilla ridiculus morbi sollicitudin eu etiam. Velit suspendisse parturient ultricies malesuada bibendum risus libero. Sem cubilia sodales massa quam eget dictum himenaeos? Efficitur eget nulla facilisis lobortis; iaculis leo massa.\n" +
        "\n" +
        "Sodales himenaeos inceptos leo bibendum suspendisse. Massa urna parturient inceptos per nibh. Nullam euismod nunc adipiscing tortor sagittis ac auctor. Dignissim nam velit; neque nullam felis mi aenean felis. Convallis morbi nisl tincidunt; justo iaculis sagittis. Primis facilisis suscipit tempor duis amet praesent. Nullam augue ex dictumst sem, taciti ipsum consequat.\n" +
        "\n" +
        "Fames parturient molestie leo hendrerit, tristique conubia nullam elementum. Sollicitudin facilisis id auctor malesuada gravida sollicitudin cras. Efficitur a lectus convallis suspendisse nulla interdum dui dictumst? Felis aptent massa ante; himenaeos posuere non mauris efficitur. Aliquam iaculis cursus senectus penatibus leo. Ut blandit felis, justo quisque enim quis suscipit. Mus maximus placerat dolor laoreet netus in parturient ultricies. Sodales nullam quam montes eleifend sem faucibus?\n" +
        "\n" +
        "Nibh mollis morbi erat finibus justo volutpat praesent elementum. Ex hendrerit suspendisse habitasse vestibulum litora orci. Scelerisque facilisi primis laoreet platea nulla maximus felis vulputate. Lectus rutrum pulvinar iaculis facilisi parturient. Pellentesque conubia non habitant ultricies sagittis eu nisi nec. Nisi enim etiam leo nulla etiam sed potenti.\n" +
        "\n" +
        "Laoreet curae massa in hac facilisis leo eu. Ac congue cursus eros condimentum molestie rutrum porta molestie. Dignissim integer magnis vulputate ultricies penatibus aenean eros. Mattis sollicitudin rutrum quisque consectetur erat. Posuere aenean suspendisse sagittis diam; egestas nostra. Enim ultrices porttitor senectus et id taciti. Neque donec lacus euismod justo fusce. Praesent enim sit quam purus bibendum ex dis.\n" +
        "\n" +
        "In hendrerit lacinia elementum eget a, diam curae elit. Bibendum odio condimentum sapien taciti elit euismod. Parturient tristique velit sapien vestibulum dictum tempor. Urna lorem euismod cursus sem euismod ligula gravida. Curabitur mi neque ullamcorper etiam, rhoncus sollicitudin. Cubilia suspendisse sed suscipit; rhoncus pulvinar per! Sem luctus a phasellus nascetur laoreet ligula lobortis efficitur.\n" +
        "\n" +
        "Praesent malesuada gravida luctus in nostra in? Bibendum sociosqu class faucibus phasellus tristique erat massa. Venenatis neque cursus nec; placerat finibus turpis. Egestas ante sapien curae venenatis a aenean. At porttitor lacus ultricies placerat sociosqu amet. Ad quisque varius torquent sapien sollicitudin consectetur. Tristique porttitor maecenas non lorem ornare. Sapien pellentesque efficitur convallis interdum pretium praesent vulputate.\n" +
        "\n" +
        "At elementum diam gravida inceptos ad. Vivamus at nullam libero pharetra interdum. Natoque facilisi felis ac condimentum; finibus parturient consequat. Amet eros nisl lacus ornare quam dolor. Sagittis consequat libero quisque netus netus, mus convallis. Accumsan a tempor quis lacus, finibus ultrices parturient. Dui consequat tortor dolor nullam et faucibus class.\n" +
        "\n" +
        "Nullam laoreet faucibus bibendum phasellus netus blandit egestas per. Auctor massa turpis aptent in varius fames. Commodo parturient consequat vestibulum ad tortor velit nunc ridiculus. Ac eget libero cras rhoncus molestie suscipit lectus vestibulum. Aliquet orci duis semper aptent per. Finibus integer ultrices commodo elementum libero. Ornare facilisi malesuada purus ex diam litora laoreet euismod. Hendrerit amet ultricies risus erat eros phasellus augue."

val lorenEntry = StringInfoEntry(text = lorem)

val lorenHyperlinkEntry = StringInfoEntry(
    text = lorem,
    linkTextToHyperlinks = mapOf("consectetuer" to "google.com", "Sapien" to "github.com"),
)

@Composable
fun lorenHyperlinkCustomStringEntry(): StringInfoEntry = StringInfoEntry(
    text = lorem,
    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
    linkStyle = MaterialTheme.typography.titleLarge,
    linkTextToHyperlinks = mapOf("consectetuer" to "google.com", "Sapien" to "github.com"),
    linkTextColor = MaterialTheme.colorScheme.primary,
    linkTextFontWeight = FontWeight.Bold,
    linkTextDecoration = TextDecoration.LineThrough,
)

val hyperlinkStringResource = StringResourceInfoEntry(
    textId = R.string.hyperlink_example,
    linkTextToHyperlinks = mapOf("LINK1" to "google.com", "LINK2" to "github.com"),
)

@Composable
fun hyperlinkCustomStringResourceEntry(): StringResourceInfoEntry = StringResourceInfoEntry(
    textId = R.string.hyperlink_example,
    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
    linkStyle = MaterialTheme.typography.titleLarge,
    linkTextToHyperlinks = mapOf("LINK1" to "google.com", "LINK2" to "github.com"),
    linkTextColor = MaterialTheme.colorScheme.primary,
    linkTextFontWeight = FontWeight.Bold,
    linkTextDecoration = TextDecoration.LineThrough,
)

val fakeDeveloper = Developer(
    name = "Fake Developer",
    organisationUrl = "www.fakeDev.com",
)

val fakeOrganization = Organization(
    name = "Fake Organization",
    url = "www.fakeOrg.com",
)

val fakeLicense = License(
    name = "Fake License",
    url = "www.fakeLicense.com",
    year = "2024",
    spdxId = "Fake",
    licenseContent = lorem,
    hash = "fake hash",
)

val fakeShortLibrary = Library(
    uniqueId = "com.google.fakeLibrary",
    artifactVersion = "1.0.0",
    name = "Fake Google Library",
    description = "This is a fake first party library used in previews.",
    website = "www.fake.com",
    developers = persistentListOf(fakeDeveloper),
    organization = fakeOrganization,
    scm = null,
    licenses = persistentSetOf(fakeLicense),
    funding = persistentSetOf(),
)

val fakeLongLibrary = Library(
    uniqueId = "com.google.fakeLibrary",
    artifactVersion = "1.0.0",
    name = "Fake Google Library Fake Google Library Fake Google Library",
    description = lorem + lorem + lorem + lorem,
    website = "www.fake.com",
    developers = persistentListOf(fakeDeveloper, fakeDeveloper, fakeDeveloper, fakeDeveloper),
    organization = fakeOrganization,
    scm = null,
    licenses = persistentSetOf(fakeLicense),
    funding = persistentSetOf(),
)