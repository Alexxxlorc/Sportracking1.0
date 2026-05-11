package onboarding.personal.model

data class UserProfile(
    val id: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastNamePaternal: String = "",
    val lastNameMaternal: String = "",
    val userName: String = "",
    val phone: String = "",
    val birthDate: String = ""
)