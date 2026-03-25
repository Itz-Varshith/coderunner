import { Link } from 'react-router-dom';
import { ArrowRight, Code2, Zap, Trophy } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

const features = [
  {
    icon: Code2,
    title: 'Practice Coding',
    description: 'Solve algorithmic problems to sharpen your programming skills',
  },
  {
    icon: Zap,
    title: 'Instant Feedback',
    description: 'Get immediate results with our fast code execution engine',
  },
  {
    icon: Trophy,
    title: 'Track Progress',
    description: 'Monitor your submissions and see your improvement over time',
  },
];

export function HomePage() {
  return (
    <div className="container px-4 py-16">
      <div className="mx-auto max-w-3xl text-center">
        <h1 className="text-4xl font-bold tracking-tight sm:text-6xl">
          Master Your
          <span className="text-primary"> Coding Skills</span>
        </h1>
        <p className="mt-6 text-lg text-muted-foreground">
          Practice solving algorithmic problems, submit your solutions, and get instant
          feedback. Perfect for interview preparation and competitive programming.
        </p>
        <div className="mt-10 flex items-center justify-center gap-4">
          <Link
            to="/problems"
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg bg-primary px-6 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Start Practicing
            <ArrowRight className="h-4 w-4" />
          </Link>
          <Link
            to="/submissions"
            className="inline-flex h-10 items-center justify-center gap-2 rounded-lg border border-input bg-background px-6 text-sm font-medium transition-colors hover:bg-muted"
          >
            View Submissions
          </Link>
        </div>
      </div>

      <div className="mx-auto mt-20 grid max-w-5xl gap-6 md:grid-cols-3">
        {features.map((feature) => (
          <Card key={feature.title} className="bg-card/50">
            <CardHeader>
              <feature.icon className="h-10 w-10 text-primary" />
              <CardTitle className="mt-4">{feature.title}</CardTitle>
            </CardHeader>
            <CardContent>
              <CardDescription className="text-base">
                {feature.description}
              </CardDescription>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
