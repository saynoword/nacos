import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Copy, Check, Download, SquareTerminal, ExternalLink } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';
import type { SkillVersionSummary } from '@/types/skill';
import { parsePipelineInfo } from '@/types/skill';
import { NPX_CLI_TARGETS } from './npx-cli-targets';

const DEFAULT_TOOL_ID = 'cursor';

export interface SkillDownloadPanelProps {
  skillName: string;
  selectedVersion: string;
  latestVersion?: string;
  versionOptions: SkillVersionSummary[];
  onVersionChange: (version: string) => void;
  onDownloadPackage: () => void;
  downloadDisabled?: boolean;
  versionSelectDisabled?: boolean;
  className?: string;
}

export function SkillDownloadPanel({
  skillName,
  selectedVersion,
  latestVersion,
  versionOptions,
  onVersionChange,
  onDownloadPackage,
  downloadDisabled,
  versionSelectDisabled,
  className,
}: SkillDownloadPanelProps) {
  const { t } = useTranslation();
  const [toolId, setToolId] = useState(DEFAULT_TOOL_ID);
  const [outputDir, setOutputDir] = useState(
    () => NPX_CLI_TARGETS.find((x) => x.id === DEFAULT_TOOL_ID)?.defaultDir ?? '~/.cursor/skills',
  );

  const selectTool = useCallback((id: string) => {
    setToolId(id);
    const next = NPX_CLI_TARGETS.find((x) => x.id === id)?.defaultDir;
    if (next) {
      setOutputDir(next);
    }
  }, []);

  const npxCommand = useMemo(() => {
    const versionPart = selectedVersion ? ` --version ${selectedVersion}` : '';
    const out = (outputDir.trim() || '~/.cursor/skills').replace(/\s+/g, ' ');
    return `npx @nacos-group/cli skill-get ${skillName}${versionPart} -o ${out}`;
  }, [skillName, selectedVersion, outputDir]);

  const handleCopyCommand = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(npxCommand);
      toast.success(t('common.cliUsage.copied'));
    } catch {
      const textarea = document.createElement('textarea');
      textarea.value = npxCommand;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      toast.success(t('common.cliUsage.copied'));
    }
  }, [npxCommand, t]);

  return (
    <Card className={cn('overflow-hidden rounded-xl border py-0 gap-0 shadow-sm', className)}>
      <CardContent className="p-4 space-y-4">
        <div className="flex items-center justify-between gap-2">
          <span className="text-sm font-semibold">{t('skill.downloadSectionTitle')}</span>
          <Select
            value={selectedVersion}
            onValueChange={onVersionChange}
            disabled={versionSelectDisabled || versionOptions.length === 0}
          >
            <SelectTrigger
              className={cn(
                'h-8 w-[min(100%,11rem)] shrink-0 rounded-lg border bg-background px-2 text-xs font-mono',
                'flex gap-1.5',
              )}
            >
              <SelectValue placeholder="—" className="truncate" />
              {latestVersion && selectedVersion === latestVersion && (
                <Badge className="shrink-0 border-0 bg-blue-100 px-1.5 py-0 text-[10px] font-medium text-blue-700 dark:bg-blue-950/60 dark:text-blue-300">
                  {t('skill.latestVersion')}
                </Badge>
              )}
            </SelectTrigger>
            <SelectContent align="end">
              {versionOptions.map((version) => {
                const vPipeline = parsePipelineInfo(version.publishPipelineInfo);
                const isVersionPendingPublish = version.status === 'reviewing' && vPipeline?.status === 'APPROVED';
                return (
                  <SelectItem key={version.version} value={version.version} textValue={version.version}>
                    <span className="flex items-center gap-2">
                      <span>{version.version}</span>
                      {latestVersion === version.version && (
                        <Badge className="border-0 bg-emerald-100 px-1 py-0 text-[10px] text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300">
                          {t('skill.latestVersion')}
                        </Badge>
                      )}
                      {version.status === 'draft' && (
                        <Badge className="border-0 bg-amber-100 px-1 py-0 text-[10px] text-amber-700 dark:bg-amber-950/50 dark:text-amber-300">
                          {t('skill.versionStatus.draft')}
                        </Badge>
                      )}
                      {version.status === 'reviewing' && (
                        <Badge
                          className={cn(
                            'border-0 px-1 py-0 text-[10px]',
                            isVersionPendingPublish
                              ? 'bg-teal-100 text-teal-700 dark:bg-teal-950/50 dark:text-teal-300'
                              : 'bg-blue-100 text-blue-700 dark:bg-blue-950/50 dark:text-blue-300',
                          )}
                        >
                          {t(isVersionPendingPublish ? 'skill.versionStatus.pendingPublish' : 'skill.versionStatus.reviewing')}
                        </Badge>
                      )}
                    </span>
                  </SelectItem>
                );
              })}
            </SelectContent>
          </Select>
        </div>

        <Button
          type="button"
          className="h-10 w-full rounded-lg bg-violet-600 text-sm font-medium text-white shadow-sm hover:bg-violet-600/90 dark:bg-violet-600 dark:hover:bg-violet-600/90"
          disabled={downloadDisabled}
          onClick={onDownloadPackage}
        >
          <Download className="mr-2 h-4 w-4" />
          {t('skill.downloadSkillPackage')}
        </Button>

        <div className="space-y-3 border-t border-border/60 pt-4">
          <div className="flex items-center justify-between gap-2">
            <p className="text-xs font-semibold text-foreground flex items-center gap-1.5">
              <SquareTerminal className="h-3.5 w-3.5 text-muted-foreground" />
              {t('skill.npxDownload')}
            </p>
            <a
              href="https://github.com/nacos-group/nacos-cli"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-0.5 text-[11px] text-muted-foreground hover:text-foreground"
            >
              {t('common.cliUsage.cliDoc')}
              <ExternalLink className="h-3 w-3" />
            </a>
          </div>

          <div className="grid grid-cols-2 gap-1.5 sm:grid-cols-4">
            {NPX_CLI_TARGETS.map((tool) => {
              const selected = toolId === tool.id;
              const Glyph = tool.Icon;
              return (
                <button
                  key={tool.id}
                  type="button"
                  onClick={() => selectTool(tool.id)}
                  className={cn(
                    'flex flex-col items-center justify-center gap-0.5 rounded-lg border bg-card px-1 py-2 text-center transition-colors',
                    'hover:bg-muted/50',
                    selected && 'border-primary ring-1 ring-primary',
                    !selected && 'border-border',
                  )}
                >
                  <span className="flex h-6 w-6 shrink-0 items-center justify-center">
                    {tool.iconSrc ? (
                      <img
                        src={tool.iconSrc}
                        alt=""
                        className="max-h-5 max-w-5 object-contain"
                        decoding="async"
                      />
                    ) : (
                      <Glyph className="h-4 w-4 text-muted-foreground" aria-hidden />
                    )}
                  </span>
                  <span className="text-[10px] font-semibold leading-tight text-muted-foreground">{tool.label}</span>
                </button>
              );
            })}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="skill-npx-output-dir" className="text-xs text-muted-foreground">
              {t('skill.outputDirectory')}
            </Label>
            <Input
              id="skill-npx-output-dir"
              value={outputDir}
              onChange={(e) => setOutputDir(e.target.value)}
              className="h-9 font-mono text-xs"
              spellCheck={false}
              autoComplete="off"
            />
          </div>

          <NpxCommandBlock command={npxCommand} onCopy={handleCopyCommand} />
        </div>
      </CardContent>
    </Card>
  );
}

function NpxCommandBlock({ command, onCopy }: { command: string; onCopy: () => void }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    onCopy();
    setCopied(true);
    window.setTimeout(() => setCopied(false), 2000);
  }, [onCopy]);

  return (
    <div className="flex items-stretch gap-2 rounded-lg border border-border/60 bg-muted/50 p-2.5">
      <pre className="min-w-0 flex-1 whitespace-pre-wrap break-all font-mono text-[11px] leading-relaxed text-foreground">
        {command}
      </pre>
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="h-8 w-8 shrink-0 text-muted-foreground hover:text-foreground"
        onClick={handleCopy}
        aria-label={t('skill.copyNpxCommand')}
      >
        {copied ? <Check className="h-3.5 w-3.5 text-emerald-600" /> : <Copy className="h-3.5 w-3.5" />}
      </Button>
    </div>
  );
}
