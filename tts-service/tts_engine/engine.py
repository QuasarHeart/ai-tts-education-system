import sys
import sys
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GPT_PARENT = os.path.join(BASE_DIR, "models", "GPT-SoVITS")
GPT_ROOT = os.path.join(GPT_PARENT, "GPT_SoVITS")
TTS_PACK = os.path.join(GPT_ROOT, "TTS_infer_pack")
ERES2NET_DIR = os.path.join(GPT_ROOT, "eres2net")

sys.path.insert(0, GPT_PARENT)
sys.path.insert(0, GPT_ROOT)
sys.path.insert(0, TTS_PACK)
sys.path.insert(0, ERES2NET_DIR)
os.chdir(GPT_PARENT)

from TTS import TTS
from typing import Dict, Any, Tuple, List
import numpy as np

class TTSEngine:
    def __init__(self, device: str = "cuda", is_half: bool = True):
        #只指定版本和设备，权重路径用默认
        custom_config = {
            "custom": {
                "version": "v2ProPlus",
                "device": device,
                "is_half": is_half
            }
        }
        self.tts = TTS(custom_config)

        self.default_inputs = {
            "text": "",
            "text_lang": "zh",
            "ref_audio_path": "",
            "aux_ref_audio_paths": [],
            "prompt_text": "",
            "prompt_lang": "zh",
            "top_k": 15,
            "top_p": 1,
            "temperature": 1,
            "text_split_method": "cut1",
            "batch_size": 1,
            "batch_threshold": 0.75,
            "split_bucket": True,
            "speed_factor": 1.0,
            "fragment_interval": 0.3,
            "seed": -1,
            "parallel_infer": True,
            "repetition_penalty": 1.35,
            "sample_steps": 32,
            "super_sampling": False,
            "return_fragment": False,
            "streaming_mode": False,
            "overlap_length": 2,
            "min_chunk_length": 16,
            "fixed_length_chunk": False,
        }

    def synthesize(self, user_inputs: Dict[str, Any]) -> Tuple[int, np.ndarray]:
        inputs = {**self.default_inputs, **user_inputs}

        for key in ["text", "text_lang", "prompt_lang", "ref_audio_path"]:
            if not inputs.get(key):
                raise ValueError(f"Missing required field: {key}")

        audio_chunks: List[np.ndarray] = []
        output_sr = None

        for sr, audio in self.tts.run(inputs):
            output_sr = sr
            if audio is not None:
                audio_chunks.append(audio)

        if not audio_chunks:
            return 16000, np.zeros(16000, dtype=np.int16)

        audio = np.concatenate(audio_chunks)
        return output_sr, audio