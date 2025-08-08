package com.example.shop.common.apis.models

data class AdminAccountDto(
    override val id: Long,
    override val email: String,
    override val enabled: Boolean,
    override val nickname: String?,
    val authority: AuthorityDto,
    val groups: List<GroupInfo>,
) : AccountDto(id, email, enabled, nickname) {
    data class MinimumGroupAuthorityDto(
        val id: Long,
        val name: String,
    )

    data class GroupInfo(
        val group: AccountGroupDto,
        val authorities: List<MinimumGroupAuthorityDto>,
    )
}
