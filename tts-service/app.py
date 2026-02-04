"""
    依赖安装
    pip install fastapi uvicorn soundfile pydub
    sudo apt-get install ffmpeg
"""


from fastapi import FastAPI, UploadFile, File, Form, BackgroundTasks
from fastapi.responses import FileResponse, JSONResponse
import uuid
import os
import shutil
import soundfile as sf
from pydub import AudioSegment  # 需要 ffmpeg
from tts_engine import TTSEngine

app = FastAPI()
engine = TTSEngine(device="cuda", is_half=True)  # 没GPU改成 cpu / False

# 简单内存任务表,建议换 Redis/DB
TASKS = {}
BASE_DIR = "ai-tts-education-system/tts-service/outputs"
os.makedirs(BASE_DIR, exist_ok=True)

def run_tts_task(task_id: str, text: str, text_lang: str, prompt_lang: str, ref_path: str, fmt: str):
    try:
        TASKS[task_id]["status"] = "running"
        sr, audio = engine.synthesize({
            "text": text,
            "text_lang": text_lang,
            "prompt_lang": prompt_lang,
            "ref_audio_path": ref_path,
        })

        wav_path = os.path.join(BASE_DIR, f"{task_id}.wav")
        sf.write(wav_path, audio, sr, format="WAV")

        if fmt == "mp3":
            mp3_path = os.path.join(BASE_DIR, f"{task_id}.mp3")
            AudioSegment.from_wav(wav_path).export(mp3_path, format="mp3")
            TASKS[task_id]["result_path"] = mp3_path
        else:
            TASKS[task_id]["result_path"] = wav_path

        TASKS[task_id]["status"] = "done"

    except Exception as e:
        TASKS[task_id]["status"] = "failed"
        TASKS[task_id]["error"] = str(e)


@app.post("/api/tts/async")
async def tts_async(
    background_tasks: BackgroundTasks,
    text: str = Form(...),
    text_lang: str = Form("zh"),
    prompt_lang: str = Form("zh"),
    format: str = Form("mp3"),  # wav / mp3
    ref_audio: UploadFile = File(...),
):
    task_id = str(uuid.uuid4())
    ref_path = os.path.join(BASE_DIR, f"{task_id}_{ref_audio.filename}")
    with open(ref_path, "wb") as f:
        f.write(await ref_audio.read())

    TASKS[task_id] = {
        "status": "queued",
        "result_path": None,
        "error": None
    }

    background_tasks.add_task(run_tts_task, task_id, text, text_lang, prompt_lang, ref_path, format)
    return {"task_id": task_id, "status": "queued"}


@app.get("/api/tts/tasks/{task_id}")
def get_task_status(task_id: str):
    if task_id not in TASKS:
        return JSONResponse(status_code=404, content={"error": "task not found"})
    return TASKS[task_id]


@app.get("/api/tts/tasks/{task_id}/download")
def download_result(task_id: str):
    if task_id not in TASKS:
        return JSONResponse(status_code=404, content={"error": "task not found"})
    if TASKS[task_id]["status"] != "done":
        return JSONResponse(status_code=400, content={"error": "task not finished"})
    return FileResponse(TASKS[task_id]["result_path"])