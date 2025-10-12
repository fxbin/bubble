package cn.fxbin.bubble.ai.lightrag.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DocumentPipelineStatusResponse
 * 示例响应
 *
 * <pre>{@code
 * {
 *   "autoscanned": false,
 *   "busy": false,
 *   "job_name": "no-file-path[1 files]",
 *   "job_start": "2025-08-27T08:50:09.958664+00:00",
 *   "docs": 1,
 *   "batchs": 1,
 *   "cur_batch": 1,
 *   "request_pending": false,
 *   "latest_message": "Document processing pipeline completed",
 *   "history_messages": [
 *     "Processing 1 document(s)",
 *     "Extracting stage 1/1: no-file-path",
 *     "Processing d-id: doc-80e35a467c44a5cff781d57b05e1b148",
 *     "Failed to extract entities and relationships: Error code: 400 - {'error': {'code': 'data_inspection_failed', 'param': None, 'message': 'Input data may contain inappropriate content.', 'type': 'data_inspection_failed'}, 'id': 'chatcmpl-9284916d-e92e-95b0-ab45-282292f0bccc', 'request_id': '9284916d-e92e-95b0-ab45-282292f0bccc'}",
 *     "Traceback (most recent call last):\n  File \"/app/lightrag/lightrag.py\", line 1337, in process_document\n    await entity_relation_task\n  File \"/app/lightrag/lightrag.py\", line 1566, in _process_entity_relation_graph\n    raise e\n  File \"/app/lightrag/lightrag.py\", line 1551, in _process_entity_relation_graph\n    chunk_results = await extract_entities(\n                    ^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/operate.py\", line 1701, in extract_entities\n    raise task.exception()\n  File \"/app/lightrag/operate.py\", line 1677, in _process_with_semaphore\n    return await _process_single_content(chunk)\n           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/operate.py\", line 1605, in _process_single_content\n    glean_result = await use_llm_func_with_cache(\n                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/utils.py\", line 1478, in use_llm_func_with_cache\n    res: str = await use_llm_func(input_text, **kwargs)\n               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/utils.py\", line 581, in wait_func\n    return await future\n           ^^^^^^^^^^^^\n  File \"/app/lightrag/utils.py\", line 365, in worker\n    result = await func(*args, **kwargs)\n             ^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/api/lightrag_server.py\", line 266, in openai_alike_model_complete\n    return await openai_complete_if_cache(\n           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/asyncio/__init__.py\", line 189, in async_wrapped\n    return await copy(fn, *args, **kwargs)\n           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/asyncio/__init__.py\", line 111, in __call__\n    do = await self.iter(retry_state=retry_state)\n         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/asyncio/__init__.py\", line 153, in iter\n    result = await action(retry_state)\n             ^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/_utils.py\", line 99, in inner\n    return call(*args, **kwargs)\n           ^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/__init__.py\", line 400, in <lambda>\n    self._add_action_func(lambda rs: rs.outcome.result())\n                                     ^^^^^^^^^^^^^^^^^^^\n  File \"/usr/local/lib/python3.11/concurrent/futures/_base.py\", line 449, in result\n    return self.__get_result()\n           ^^^^^^^^^^^^^^^^^^^\n  File \"/usr/local/lib/python3.11/concurrent/futures/_base.py\", line 401, in __get_result\n    raise self._exception\n  File \"/root/.local/lib/python3.11/site-packages/tenacity/asyncio/__init__.py\", line 114, in __call__\n    result = await fn(*args, **kwargs)\n             ^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/app/lightrag/llm/openai.py\", line 187, in openai_complete_if_cache\n    response = await openai_async_client.chat.completions.create(\n               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/openai/resources/chat/completions/completions.py\", line 2544, in create\n    return await self._post(\n           ^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/openai/_base_client.py\", line 1791, in post\n    return await self.request(cast_to, opts, stream=stream, stream_cls=stream_cls)\n           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n  File \"/root/.local/lib/python3.11/site-packages/openai/_base_client.py\", line 1591, in request\n    raise self._make_status_error_from_response(err.response) from None\nopenai.BadRequestError: Error code: 400 - {'error': {'code': 'data_inspection_failed', 'param': None, 'message': 'Input data may contain inappropriate content.', 'type': 'data_inspection_failed'}, 'id': 'chatcmpl-9284916d-e92e-95b0-ab45-282292f0bccc', 'request_id': '9284916d-e92e-95b0-ab45-282292f0bccc'}\n",
 *     "Failed to extract document 1/1: no-file-path",
 *     "Document processing pipeline completed"
 *   ],
 *   "update_status": {}
 * }
 * }</pre>
 *
 * @author fxbin
 * @version v1.0
 * @since 2025/8/27 17:20
 */
@Data
public class DocumentPipelineStatusResponse implements Serializable {

    private boolean autoscanned;

    private boolean busy;

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("job_start")
    private String jobStart;

    private int docs;

    private int batchs;

    @JsonProperty("cur_batch")
    private int curBatch;

    @JsonProperty("request_pending")
    private boolean requestPending;

    @JsonProperty("latest_message")
    private String latestMessage;

    @JsonProperty("history_messages")
    private List<String> historyMessages;

    @JsonProperty("update_status")
    private Object updateStatus;
}
