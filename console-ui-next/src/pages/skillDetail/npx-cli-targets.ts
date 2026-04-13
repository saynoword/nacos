import type { LucideIcon } from 'lucide-react';
import {
  Box,
  Orbit,
  Braces,
  Bot,
  Code2,
  MousePointer2,
  Hexagon,
  Sparkles,
} from 'lucide-react';

/**
 * NPX 目标工具条配置。
 *
 * - `iconSrc`：可选。把图标放到 `console-ui-next/public/` 下，这里写**站点根路径**。
 *   例：文件为 `public/img/skill-cli/cursor.svg` → `iconSrc: '/img/skill-cli/cursor.svg'`。
 * - `Icon`：未配置 `iconSrc` 时使用的 Lucide 占位图标；可改成任意 `lucide-react` 图标组件。
 */
export interface NpxCliTarget {
  id: string;
  label: string;
  defaultDir: string;
  iconSrc?: string;
  Icon: LucideIcon;
}

export const NPX_CLI_TARGETS: NpxCliTarget[] = [
  { id: 'copaw', label: 'CoPaw', defaultDir: '~/.copaw/skills', Icon: Box },
  { id: 'openclaw', label: 'OpenClaw', defaultDir: '~/.openclaw/skills', Icon: Orbit },
  { id: 'qoder', label: 'Qoder', defaultDir: '~/.qoder/skills', Icon: Braces },
  { id: 'claude', label: 'Claude', defaultDir: '~/.claude/skills', Icon: Bot },
  { id: 'codex', label: 'Codex', defaultDir: '~/.codex/skills', Icon: Code2 },
  { id: 'cursor', label: 'Cursor', defaultDir: '~/.cursor/skills', Icon: MousePointer2 },
  { id: 'kiro', label: 'Kiro', defaultDir: '~/.kiro/skills', Icon: Hexagon },
  { id: 'lingma', label: 'Lingma', defaultDir: '~/.lingma/skills', Icon: Sparkles },
];
