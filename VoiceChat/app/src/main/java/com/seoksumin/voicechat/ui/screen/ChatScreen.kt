package com.seoksumin.voicechat.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.seoksumin.voicechat.data.remote.RetrofitClient
import com.seoksumin.voicechat.data.remote.dto.VoiceItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


enum class RecordStatusFilter(val label: String) {
    ALL("전체"),
    READY("대화 가능"),
    TRAINING("복원 중"),
    FAILED("실패"),
    CREATED("생성됨")
}

enum class RecordSortType(val label: String) {
    LATEST("최신순"),
    NAME("이름순"),
    STATUS("상태순")
}

@Composable
fun ChatScreen(
    onGoUpload: () -> Unit,
    onClickRecord: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val api = remember { RetrofitClient.create(context) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(RecordStatusFilter.ALL) }
    var selectedSort by rememberSaveable { mutableStateOf(RecordSortType.LATEST) }

    var records by remember { mutableStateOf<List<VoiceItemDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadVoices() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null

                val result = withContext(Dispatchers.IO) {
                    api.getVoices()
                }

                records = result
            } catch (e: Exception) {
                errorMessage = "기록을 불러오지 못했습니다."
                records = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadVoices()
    }

    val filteredRecords = records
        .filter { item ->
            val matchesSearch = item.personName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                RecordStatusFilter.ALL -> true
                RecordStatusFilter.READY -> item.status == "READY"
                RecordStatusFilter.TRAINING -> item.status == "TRAINING"
                RecordStatusFilter.FAILED -> item.status == "FAILED"
                RecordStatusFilter.CREATED -> item.status == "CREATED"
            }

            matchesSearch && matchesFilter
        }
        .let { list ->
            when (selectedSort) {
                RecordSortType.LATEST -> list.sortedByDescending { it.createdAt }
                RecordSortType.NAME -> list.sortedBy { it.personName }
                RecordSortType.STATUS -> list.sortedBy { statusLabel(it.status) }
            }
        }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onGoUpload
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("나의 기록", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(14.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
            Spacer(Modifier.height(14.dp))

            FilterRow(
                selectedFilter = selectedFilter,
                onSelectFilter = { selectedFilter = it },
                selectedSort = selectedSort,
                onSelectSort = { selectedSort = it }
            )
            Spacer(Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    ErrorState(
                        message = errorMessage!!,
                        onRetry = { loadVoices() }
                    )
                }

                records.isEmpty() -> {
                    EmptyState(onNewRestore = onGoUpload)
                }

                filteredRecords.isEmpty() -> {
                    EmptyResultState(
                        onClearFilter = {
                            searchQuery = ""
                            selectedFilter = RecordStatusFilter.ALL
                            selectedSort = RecordSortType.LATEST
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(filteredRecords, key = { it.voiceId }) { item ->
                            RecordCard(
                                item = item,
                                onClick = { onClickRecord(item.voiceId) },
                                onStartChat = { onClickRecord(item.voiceId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("이름으로 검색") },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun FilterRow(
    selectedFilter: RecordStatusFilter,
    onSelectFilter: (RecordStatusFilter) -> Unit,
    selectedSort: RecordSortType,
    onSelectSort: (RecordSortType) -> Unit
) {
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box {
            AssistChip(
                onClick = { filterMenuExpanded = true },
                label = { Text(selectedFilter.label) }
            )

            DropdownMenu(
                expanded = filterMenuExpanded,
                onDismissRequest = { filterMenuExpanded = false }
            ) {
                RecordStatusFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = { Text(filter.label) },
                        onClick = {
                            onSelectFilter(filter)
                            filterMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(10.dp))

        Box {
            AssistChip(
                onClick = { sortMenuExpanded = true },
                label = { Text("${selectedSort.label} ▼") }
            )

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                RecordSortType.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = { Text(sort.label) },
                        onClick = {
                            onSelectSort(sort)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    item: VoiceItemDto,
    onClick: () -> Unit,
    onStartChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(item.createdAt),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(item.personName, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = statusLabel(item.status),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onStartChat,
                    shape = RoundedCornerShape(20.dp),
                    enabled = item.status == "READY"
                ) {
                    Text(
                        when (item.status) {
                            "READY" -> "대화 시작"
                            "TRAINING" -> "복원 중"
                            "FAILED" -> "다시 시도 필요"
                            "CREATED" -> "업로드 필요"
                            else -> "상태 확인"
                        }
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(statusEmoji(item.status))
            }
        }
    }
}

@Composable
private fun EmptyState(
    onNewRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "아직 저장된 목소리가 없어요",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "새로운 음성 복원을 시작해볼까요?",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNewRestore,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("새로운 음성 복원하기")
        }
    }
}

@Composable
private fun EmptyResultState(
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "조건에 맞는 기록이 없어요",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "검색어나 필터를 바꿔보세요.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onClearFilter,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("필터 초기화")
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

private fun statusLabel(status: String): String {
    return when (status) {
        "READY" -> "대화 가능"
        "TRAINING" -> "복원 중"
        "FAILED" -> "실패"
        "CREATED" -> "생성됨"
        else -> status
    }
}

private fun statusEmoji(status: String): String {
    return when (status) {
        "READY" -> "🎙"
        "TRAINING" -> "⏳"
        "FAILED" -> "⚠"
        "CREATED" -> "🆕"
        else -> "IMG"
    }
}

private fun formatDate(raw: String): String {
    return raw.replace("T", " ").replace("Z", "")
}