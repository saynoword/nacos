import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Tag, ArrowRight, Trash2, Pencil, Check, X, AlertCircle } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { isValidLabelKey } from '@/pages/agentSpecManagement/components/label-utils';

interface LabelManagerDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Full label->version mapping */
  allLabels: Record<string, string>;
  /** List of available version strings to assign */
  availableVersions: string[];
  onSave: (labels: Record<string, string>) => Promise<void>;
}

interface LabelDraft {
  key: string;
  version: string;
  isNew?: boolean;
}

export function LabelManagerDialog({
  open,
  onOpenChange,
  allLabels,
  availableVersions,
  onSave,
}: LabelManagerDialogProps) {
  const { t } = useTranslation();
  const [saving, setSaving] = useState(false);
  const [drafts, setDrafts] = useState<LabelDraft[]>([]);
  const [editingKey, setEditingKey] = useState<string | null>(null);
  const [editingVersion, setEditingVersion] = useState('');
  const [newLabelKey, setNewLabelKey] = useState('');
  const [newLabelVersion, setNewLabelVersion] = useState('');
  const [error, setError] = useState('');

  // Sync drafts when dialog opens
  useEffect(() => {
    if (!open) return;
    const entries = Object.entries(allLabels).map(([key, version]) => ({
      key,
      version,
    }));
    // Ensure 'latest' comes first
    entries.sort((a, b) => {
      if (a.key === 'latest') return -1;
      if (b.key === 'latest') return 1;
      return a.key.localeCompare(b.key);
    });
    setDrafts(entries);
    setEditingKey(null);
    setNewLabelKey('');
    setNewLabelVersion('');
    setError('');
  }, [open, allLabels]);

  const existingKeys = useMemo(() => drafts.map((d) => d.key), [drafts]);

  // Online versions first for better UX in selectors
  const versionOptions = useMemo(() => [...availableVersions], [availableVersions]);

  const handleStartEdit = (key: string) => {
    const draft = drafts.find((d) => d.key === key);
    if (!draft) return;
    setEditingKey(key);
    setEditingVersion(draft.version);
  };

  const handleConfirmEdit = (key: string) => {
    if (!editingVersion) return;
    setDrafts((prev) =>
      prev.map((d) => (d.key === key ? { ...d, version: editingVersion } : d)),
    );
    setEditingKey(null);
    setEditingVersion('');
  };

  const handleCancelEdit = () => {
    setEditingKey(null);
    setEditingVersion('');
  };

  const handleDelete = (key: string) => {
    if (key === 'latest') return;
    setDrafts((prev) => prev.filter((d) => d.key !== key));
  };

  const handleAddLabel = () => {
    const trimmedKey = newLabelKey.trim();
    if (!trimmedKey) {
      setError(t('common.versionLabels.keyRequired'));
      return;
    }
    if (!isValidLabelKey(trimmedKey, existingKeys)) {
      if (existingKeys.includes(trimmedKey)) {
        setError(t('common.versionLabels.keyDuplicate'));
      } else {
        setError(t('common.versionLabels.keyInvalid'));
      }
      return;
    }
    if (!newLabelVersion) {
      setError(t('common.labelManager.versionRequired'));
      return;
    }
    setDrafts((prev) => [...prev, { key: trimmedKey, version: newLabelVersion, isNew: true }]);
    setNewLabelKey('');
    setNewLabelVersion('');
    setError('');
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      const result: Record<string, string> = {};
      for (const d of drafts) {
        result[d.key] = d.version;
      }
      await onSave(result);
      onOpenChange(false);
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Tag className="h-4 w-4" />
            {t('common.labelManager.title')}
          </DialogTitle>
          <DialogDescription>
            {t('common.labelManager.description')}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-3">
          {/* Existing labels list */}
          {drafts.length > 0 && (
            <div className="max-h-[300px] overflow-y-auto space-y-1.5">
              {drafts.map((draft) => {
                const isLatest = draft.key === 'latest';
                const isEditing = editingKey === draft.key;

                return (
                  <div
                    key={draft.key}
                    className="flex items-center gap-2 px-3 py-2 rounded-md border border-border/60 bg-muted/30 group"
                  >
                    {/* Label name */}
                    <span className="flex items-center gap-1.5 min-w-0 shrink-0">
                      <span className="text-sm font-mono font-medium truncate">{draft.key}</span>
                      {isLatest && (
                        <Badge className="bg-emerald-100 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300 text-[10px] px-1 py-0 border-0 shrink-0">
                          {t('common.labelManager.protected')}
                        </Badge>
                      )}
                    </span>

                    <ArrowRight className="h-3 w-3 text-muted-foreground shrink-0" />

                    {isEditing ? (
                      <>
                        <Select value={editingVersion} onValueChange={setEditingVersion}>
                          <SelectTrigger className="h-7 text-xs flex-1 min-w-[100px]">
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            {versionOptions.map((v) => (
                              <SelectItem key={v} value={v}>
                                <span className="font-mono text-xs">{v}</span>
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 shrink-0"
                          onClick={() => handleConfirmEdit(draft.key)}
                        >
                          <Check className="h-3 w-3 text-emerald-600" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 shrink-0"
                          onClick={handleCancelEdit}
                        >
                          <X className="h-3 w-3" />
                        </Button>
                      </>
                    ) : (
                      <>
                        <span className="text-xs font-mono text-muted-foreground flex-1 min-w-0 truncate">
                          {draft.version}
                        </span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={() => handleStartEdit(draft.key)}
                        >
                          <Pencil className="h-3 w-3" />
                        </Button>
                        {!isLatest && (
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-6 w-6 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity text-destructive hover:text-destructive"
                            onClick={() => handleDelete(draft.key)}
                          >
                            <Trash2 className="h-3 w-3" />
                          </Button>
                        )}
                      </>
                    )}
                  </div>
                );
              })}
            </div>
          )}

          {drafts.length === 0 && (
            <p className="text-sm text-muted-foreground text-center py-4">
              {t('common.versionLabels.noLabels')}
            </p>
          )}

          {/* Add new label */}
          <div className="border-t border-border/60 pt-3 space-y-2">
            <p className="text-xs font-medium text-muted-foreground">{t('common.labelManager.addLabel')}</p>
            <div className="flex items-center gap-2">
              <Input
                value={newLabelKey}
                onChange={(e) => {
                  setNewLabelKey(e.target.value);
                  setError('');
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddLabel();
                  }
                }}
                placeholder={t('common.versionLabels.keyPlaceholder')}
                className="h-8 text-xs flex-1"
              />
              <ArrowRight className="h-3 w-3 text-muted-foreground shrink-0" />
              <Select value={newLabelVersion} onValueChange={setNewLabelVersion}>
                <SelectTrigger className="h-8 text-xs flex-1 min-w-[100px]">
                  <SelectValue placeholder={t('common.versionLabels.valuePlaceholder')} />
                </SelectTrigger>
                <SelectContent>
                  {versionOptions.map((v) => (
                    <SelectItem key={v} value={v}>
                      <span className="font-mono text-xs">{v}</span>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button
                variant="outline"
                size="icon"
                className="h-8 w-8 shrink-0"
                onClick={handleAddLabel}
              >
                <Plus className="h-3.5 w-3.5" />
              </Button>
            </div>
            {error && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <AlertCircle className="h-3 w-3" />
                {error}
              </p>
            )}
          </div>

          {/* Info about latest protection */}
          <p className="text-[11px] text-muted-foreground flex items-center gap-1">
            <AlertCircle className="h-3 w-3 shrink-0" />
            {t('common.labelManager.latestProtectionHint')}
          </p>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={saving}>
            {t('common.cancel')}
          </Button>
          <Button onClick={handleSave} disabled={saving}>
            {saving ? t('common.loading') : t('common.save')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
